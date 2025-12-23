/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

//! Proc-macro helpers for DynamoDB entities used by `wrzasqpl-commons-aws`.
//!
//! This crate exposes the `DynamoEntity` derive macro which generates implementations and helpers needed to work with
//! the `DynamoDbEntity` trait from the `wrzasqpl-commons-aws` crate.
//!
//! Quick start
//! - Put `#[derive(DynamoEntity)]` on a struct that models a DynamoDB item.
//! - Mark the hash key and sort key fields, or rely on defaults:
//!   - Hash key: field marked with `#[hash_key]`, or a field named `id`.
//!   - Sort key: field marked with `#[sort_key]`, or a field named `sk`.
//! - Optional attribute options:
//!   - `#[hash_key(name = "...")]`/`#[sort_key(name = "...")]` - overrides name used for DynamoDB attribute.
//!   - `#[hash_key(prefix = "...")]` – prefixes hash values (useful to namespace keys).
//!   - `#[sort_key(const = "...")]` – uses a constant sort key value.
//!   - `#[sort_key(prefix = "...")]` – enables `begins_with` for queries on that prefix.
//!
//! Generated pieces
//! - Implements `::wrzasqpl_commons_aws::DynamoDbEntity` for your type.
//! - Emits a companion `Key` struct named `YourTypeKey { hk, sk }` with the same
//!   key field names and types as your struct.
//! - Adds convenience constructors:
//!   - `new(hash, sort, other_fields...)` – when sort key is not const.
//!   - `new(hash, other_fields...)` and `key_from_hash(hash)` – when sort key is const.
//! - If `#[hash_key(prefix = "...")]` is used, `new(...)` applies that prefix.
//! - If `#[sort_key(prefix = "...")]` is used, `handle_query(...)` adds a
//!   `begins_with` condition for the sort key prefix.
//! - If the struct contains the sort key field and it is declared as const,
//!   `handle_save(...)` will enforce that constant on save.
//!
//! Examples
//!
//! Basic entity with explicit key attributes
//! ```
//! use serde::{Deserialize, Serialize};
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
//! struct Order {
//!     #[hash_key]
//!     id: String,
//!     #[sort_key]
//!     sk: String,
//!     status: String,
//! }
//! ```
//!
//! Using defaults (`id` for hash key, `sk` for sort key)
//! ```
//! use serde::{Deserialize, Serialize};
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
//! struct Invoice {
//!     id: String, // used as hash key by default
//!     sk: String, // used as sort key by default
//!     amount_cents: i64,
//! }
//! ```
//!
//! Constant sort key and hash prefix
//! ```
//! use serde::{Deserialize, Serialize};
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
//! struct Profile {
//!     #[hash_key(prefix = "USER#", name = "profileId")]
//!     profile_id: String,
//!     #[sort_key(const = "PROFILE")]
//!     sk: String, // value enforced to "PROFILE"
//!     display_name: String,
//! }
//!
//! // With const sort key, the macro provides:
//! // - Profile::new(hash, display_name)
//! // - ProfileKey::from(hash) -> ProfileKey
//! ```

use proc_macro2::{Span, TokenStream};
use quote::{format_ident, quote};
use syn::meta::ParseNestedMeta;
use syn::spanned::Spanned;
use syn::{Data, DeriveInput, Error as SynError, Fields, Ident, LitStr, Meta, Result as SynResult, Type};

// I have to admit I regret merging this Derive macro as a little pre-mature review - not sure if `prefix` and/or
// `const` features should be part of this layer. Can be very tricky to handle all the cases of transformation between
// persistent data and model representation. Not to mention all the possible cloning/borrow cases of custom types in
// constructor. In next major version these two properties should be most likely dropped, unless we come out with some
// robust implementation. I don't want partial solutions.

struct KeyField {
    ident: Ident,
    name: Ident,
    r#type: Type,
    const_value: Option<String>,
    prefix: Option<String>,
}

fn unnest_literal(nested: ParseNestedMeta) -> SynResult<Option<String>> {
    Ok(Some(nested.value()?.parse::<LitStr>()?.value()))
}

fn build_producer(key: &KeyField, variable_neme: Ident) -> TokenStream {
    if let Some(value) = &key.const_value {
        let lit = LitStr::new(value, Span::call_site());

        quote! { #lit.to_string() }
    } else if let Some(prefix) = &key.prefix {
        let lit = LitStr::new(prefix, Span::call_site());

        // prefix makes sanse only if key type is Into<String> anyway
        quote! { format!(concat!(#lit, "{}"), #variable_neme.to_string()) }
    } else {
        quote! { #variable_neme.clone() }
    }
}

pub(crate) fn derive_dynamo_entity_impl(input: DeriveInput) -> TokenStream {
    let struct_ident = input.ident.clone();
    let input_span = input.span();

    // Only support named-field structs
    let Data::Struct(data_struct) = input.data else {
        return SynError::new(input_span, "DynamoEntity can only be derived for structs").to_compile_error();
    };

    let Fields::Named(fields) = data_struct.fields else {
        return SynError::new(
            input_span,
            "DynamoEntity requires named fields (struct with field names)",
        )
        .to_compile_error();
    };

    // Discover hash_key and sort_key fields
    let mut hash_key: Option<KeyField> = None;
    let mut sort_key: Option<KeyField> = None;

    for field in fields.named.iter() {
        let Some(ident) = field.ident.clone() else { continue };
        let mut is_hash = false;
        let mut is_sort = false;
        let mut const_value: Option<String> = None;
        let mut prefix: Option<String> = None;
        let mut name: Ident = ident.clone();

        for attr in &field.attrs {
            let path = attr.path();

            if path.is_ident("hash_key") {
                is_hash = true;

                // Parse optional meta: hash_key(prefix = "...")
                if let Meta::List(list) = &attr.meta {
                    let _ = list.parse_nested_meta(|nested| {
                        if nested.path.is_ident("prefix") {
                            prefix = unnest_literal(nested)?;
                        } else if nested.path.is_ident("name") {
                            name = unnest_literal(nested)?
                                .map(|value| Ident::new(value.as_str(), Span::call_site()))
                                .unwrap_or(name.clone());
                        }
                        Ok(())
                    });
                }
            }

            if path.is_ident("sort_key") {
                is_sort = true;

                // Parse optional meta: sort_key(const = "...", prefix = "...")
                if let Meta::List(list) = &attr.meta {
                    let _ = list.parse_nested_meta(|nested| {
                        if nested.path.is_ident("const") {
                            const_value = unnest_literal(nested)?;
                        } else if nested.path.is_ident("prefix") {
                            prefix = unnest_literal(nested)?;
                        } else if nested.path.is_ident("name") {
                            name = unnest_literal(nested)?
                                .map(|value| Ident::new(value.as_str(), Span::call_site()))
                                .unwrap_or(name.clone());
                        }
                        Ok(())
                    });
                }
            }
        }

        if is_hash || (hash_key.is_none() && ident == "id") {
            // Default to field named "id" if not explicitly marked and no explicit hash_key found yet
            hash_key = Some(KeyField {
                ident,
                name,
                r#type: field.ty.clone(),
                const_value,
                prefix,
            });
        } else if is_sort || (sort_key.is_none() && ident == "sk") {
            // Default to field named "sk" if not explicitly marked and no explicit sort_key found yet
            sort_key = Some(KeyField {
                ident,
                name,
                r#type: field.ty.clone(),
                const_value,
                prefix,
            });
        }
    }

    let Some(hash_key) = hash_key else {
        return SynError::new(
            input_span,
            "DynamoEntity: missing hash key. Mark a field with #[hash_key] or include a field named 'id'.",
        )
        .to_compile_error();
    };

    // Parse key_attrs from derive macro attributes
    let mut key_attrs = Vec::new();
    for attr in &input.attrs {
        if attr.path().is_ident("key_attrs")
            && let Meta::List(list) = &attr.meta
        {
            key_attrs.push(list.tokens.clone());
        }
    }

    // hk - hash_key, sk - sort_key, nk - non-key

    let hash_producer = build_producer(&hash_key, Ident::new("hash", Span::call_site()));

    // Names and idents
    let hk_ident = hash_key.ident;
    let hk_type = hash_key.r#type;
    let hk_name_str = LitStr::new(&hash_key.name.to_string(), Span::call_site());

    let mut sk_definition = quote! {};
    let mut sk_value = quote! {};
    let mut handle_query_impl = quote! {};
    let mut handle_save_const_impl = quote! {};
    let mut key_from_hash_impl = quote! {};
    let mut new_constructor_impl = quote! {};

    let key_ident = format_ident!("{}Key", struct_ident);

    if let Some(sort_key) = sort_key {
        let sort_producer = build_producer(&sort_key, Ident::new("sort", Span::call_site()));

        let sk_ident = sort_key.ident;
        let sk_type = sort_key.r#type;
        let sk_name_str = LitStr::new(&sort_key.name.to_string(), Span::call_site());

        // sk expression (const or field)
        let sk_expr = if let Some(value) = &sort_key.const_value {
            let lit = LitStr::new(value, Span::call_site());
            quote! { #lit.to_string() }
        } else {
            quote! { self.#sk_ident.clone() }
        };

        // Non-key fields list for constructors based on parsed fields
        let (nk_idents, nk_types): (Vec<Ident>, Vec<Type>) = fields
            .named
            .iter()
            .cloned()
            .filter_map(|field| {
                let ident = field.ident.clone()?;
                if ident == hk_ident || ident == sk_ident {
                    None
                } else {
                    Some((ident, field.ty.clone()))
                }
            })
            .unzip();

        sk_definition = quote! { pub #sk_ident: #sk_type, };
        sk_value = quote! { #sk_ident: #sk_expr, };

        // Implement handle_query to add begins_with for optional sk prefix
        if let Some(prefix) = &sort_key.prefix {
            let lit = LitStr::new(prefix, Span::call_site());
            let sk_name_str = sk_name_str.clone();
            handle_query_impl = quote! {
                fn handle_query<HashKeyType: ::serde::Serialize>(
                    _hash_key: &HashKeyType,
                    request: ::wrzasqpl_commons_aws::reexports::QueryFluentBuilder,
                ) -> ::wrzasqpl_commons_aws::reexports::QueryFluentBuilder {
                    use ::wrzasqpl_commons_aws::reexports::AttributeValue::S;
                    request
                        .key_condition_expression("#attr = :val AND begins_with(#sk, :sk)")
                        .expression_attribute_names("#sk", #sk_name_str)
                        .expression_attribute_values(":sk", S(#lit.to_string()))
                }
            };
        }

        if sort_key.const_value.is_some() {
            // When sort key is const and the struct contains the sk field, enforce it on save
            handle_save_const_impl = quote! {
                fn handle_save(
                    &mut self,
                    request: ::wrzasqpl_commons_aws::reexports::PutItemFluentBuilder,
                ) -> ::wrzasqpl_commons_aws::reexports::PutItemFluentBuilder {
                    self.#sk_ident = #sort_producer;
                    request
                }
            };

            // Optional inherent impl constructor for keys when sk is const
            key_from_hash_impl = quote! {
                impl From<#hk_type> for #key_ident {
                    fn from(hash: #hk_type) -> Self {
                        Self {
                            #hk_ident: #hash_producer,
                            #sk_ident: #sort_producer,
                        }
                    }
                }
            };

            // Inherent impl: generic constructor `new(...)` to build an entity from hash (+ sk if needed) and non-key fields
            // sk is constant, omit from params
            new_constructor_impl = quote! {
                impl #struct_ident {
                    pub fn new(hash: #hk_type, #( #nk_idents: #nk_types ),* ) -> Self {
                        Self {
                            #hk_ident: #hash_producer,
                            #sk_ident: #sort_producer,
                            #( #nk_idents ),*
                        }
                    }
                }
            };
        } else {
            new_constructor_impl = quote! {
                impl #struct_ident {
                    pub fn new(hash: #hk_type, sort: #sk_type, #( #nk_idents: #nk_types ),* ) -> Self {
                        Self {
                            #hk_ident: #hash_producer,
                            #sk_ident: #sort_producer,
                            #( #nk_idents ),*
                        }
                    }
                }
            };
        }
    } else {
        // Non-key fields list for constructors based on parsed fields
        let (nk_idents, nk_types): (Vec<Ident>, Vec<Type>) = fields
            .named
            .iter()
            .cloned()
            .filter_map(|field| {
                let ident = field.ident.clone()?;
                if ident == hk_ident {
                    None
                } else {
                    Some((ident, field.ty.clone()))
                }
            })
            .unzip();

        // for hash-only keys we can always implement From<>
        key_from_hash_impl = quote! {
            impl From<#hk_type> for #key_ident {
                fn from(hash: #hk_type) -> Self {
                    Self {
                        #hk_ident: #hash_producer,
                    }
                }
            }
        };

        // prefix can only be used if hash key is String
        new_constructor_impl = quote! {
            impl #struct_ident {
                pub fn new(hash: #hk_type, #( #nk_idents: #nk_types ),* ) -> Self {
                    Self {
                        #hk_ident: #hash_producer,
                        #( #nk_idents ),*
                    }
                }
            }
        };
    }

    quote! {
        #[allow(non_camel_case_types)]
        #[derive(serde::Serialize, serde::Deserialize, Debug, Clone)]
        #(#[#key_attrs])*
        pub struct #key_ident {
            pub #hk_ident: #hk_type,
            #sk_definition
        }

        impl ::wrzasqpl_commons_aws::DynamoDbEntity<'_> for #struct_ident {
            type Key = #key_ident;

            fn hash_key_name() -> ::std::string::String {
                #hk_name_str.to_string()
            }

            fn build_key(&self) -> Self::Key {
                Self::Key {
                    #hk_ident: self.#hk_ident.clone(),
                    #sk_value
                }
            }

            #handle_query_impl
            #handle_save_const_impl
        }

        #key_from_hash_impl
        #new_constructor_impl
    }
}

#[cfg(test)]
mod tests {
    use super::derive_dynamo_entity_impl;
    use proc_macro2::TokenStream;
    use quote::{ToTokens, quote};
    use syn::{DeriveInput, Fields, File, FnArg, ImplItem, Item, ItemImpl, ItemStruct, Type, parse2};

    fn expand_to_file(input: TokenStream) -> File {
        let input: DeriveInput = parse2(input).expect("failed to parse input");
        let tokens = derive_dynamo_entity_impl(input);
        parse2(tokens).expect("generated tokens should parse")
    }

    fn expand_raw(input: TokenStream) -> String {
        let input: DeriveInput = parse2(input).expect("failed to parse input");
        derive_dynamo_entity_impl(input).to_string()
    }

    fn find_struct<'a>(items: &'a [Item], name: &str) -> &'a ItemStruct {
        items
            .iter()
            .find_map(|item| match item {
                Item::Struct(item_struct) if item_struct.ident == name => Some(item_struct),
                _ => None,
            })
            .unwrap_or_else(|| panic!("struct `{name}` not found in output"))
    }

    fn find_inherent_impl<'a>(items: &'a [Item], name: &str) -> Vec<&'a ItemImpl> {
        items
            .iter()
            .filter_map(|item| match item {
                Item::Impl(item_impl)
                    if item_impl.trait_.is_none()
                        && matches!(item_impl.self_ty.as_ref(), Type::Path(path) if path.path.is_ident(name)) =>
                {
                    Some(item_impl)
                }
                _ => None,
            })
            .collect()
    }

    fn find_trait_impl<'a>(items: &'a [Item], name: &str) -> &'a ItemImpl {
        items
            .iter()
            .find_map(|item| match item {
                Item::Impl(item_impl)
                    if item_impl.trait_.is_some()
                        && matches!(item_impl.self_ty.as_ref(), Type::Path(path) if path.path.is_ident(name)) =>
                {
                    Some(item_impl)
                }
                _ => None,
            })
            .unwrap_or_else(|| panic!("trait impl for `{name}` not found"))
    }

    fn has_fn(impl_block: &ItemImpl, name: &str) -> bool {
        impl_block
            .items
            .iter()
            .any(|item| matches!(item, ImplItem::Fn(func) if func.sig.ident == name))
    }

    #[test]
    fn hash_key_only() {
        let file = expand_to_file(quote! {
            struct Customer {
                #[hash_key]
                customer_id: String,
                name: String,
            }
        });

        let key_struct = find_struct(&file.items, "CustomerKey");
        let fields: Vec<_> = match &key_struct.fields {
            Fields::Named(fields) => fields
                .named
                .iter()
                .map(|f| f.ident.as_ref().unwrap().to_string())
                .collect(),
            fields => panic!("unexpected fields generated: {fields:?}"),
        };
        assert_eq!(fields, vec!["customer_id"]);

        let entity_impl = find_trait_impl(&file.items, "Customer");
        assert!(has_fn(entity_impl, "hash_key_name"));
        assert!(has_fn(entity_impl, "build_key"));
        assert!(!has_fn(entity_impl, "handle_query"));
        assert!(!has_fn(entity_impl, "handle_save"));
    }

    #[test]
    fn defaults_detect_id_and_sk_fields() {
        let file = expand_to_file(quote! {
            struct Invoice {
                id: String,
                sk: String,
                amount_cents: i64,
            }
        });

        let key_struct = find_struct(&file.items, "InvoiceKey");
        let fields: Vec<_> = match &key_struct.fields {
            Fields::Named(fields) => fields
                .named
                .iter()
                .map(|f| f.ident.as_ref().unwrap().to_string())
                .collect(),
            fields => panic!("unexpected fields generated: {fields:?}"),
        };
        assert_eq!(fields, vec!["id", "sk"]);

        find_inherent_impl(&file.items, "Invoice")
            .iter()
            .flat_map(|imp| imp.items.iter())
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "new" => Some(func),
                _ => None,
            })
            .expect("new() constructor missing");

        let entity_impl = find_trait_impl(&file.items, "Invoice");
        assert!(has_fn(entity_impl, "hash_key_name"));
        assert!(has_fn(entity_impl, "build_key"));
        assert!(!has_fn(entity_impl, "handle_query"));
        assert!(!has_fn(entity_impl, "handle_save"));
    }

    #[test]
    fn hash_prefix_applies_to_constructor_and_key() {
        let file = expand_to_file(quote! {
            struct Tenant {
                #[hash_key(prefix = "TENANT#")]
                id: String,
                #[sort_key]
                sk: String,
                label: String,
            }
        });

        let new_fn_body = find_inherent_impl(&file.items, "Tenant")
            .iter()
            .flat_map(|imp| imp.items.iter())
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "new" => Some(func.block.to_token_stream().to_string()),
                _ => None,
            })
            .expect("new() body not found");

        assert!(
            new_fn_body.contains("concat ! (\"TENANT#\" , \"{}\")"),
            "expected hash prefix formatting in constructor: {new_fn_body}"
        );

        let entity_impl = find_trait_impl(&file.items, "Tenant");
        let build_key_body = entity_impl
            .items
            .iter()
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "build_key" => Some(func.block.to_token_stream().to_string()),
                _ => None,
            })
            .expect("build_key body missing");
        assert!(build_key_body.contains("self . sk . clone"));
    }

    #[test]
    fn const_sort_key_generates_short_constructor_and_key_helper() {
        let file = expand_to_file(quote! {
            struct Profile {
                #[hash_key(prefix = "USER#")]
                id: String,
                #[sort_key(const = "PROFILE")]
                sk: String,
                display_name: String,
            }
        });

        // new(hash, display_name) should only require the hash argument
        let new_fn = find_inherent_impl(&file.items, "Profile")
            .iter()
            .flat_map(|imp| imp.items.iter())
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "new" => Some(func),
                _ => None,
            })
            .expect("new() constructor missing");

        let params: Vec<_> = new_fn
            .sig
            .inputs
            .iter()
            .filter_map(|arg| match arg {
                FnArg::Typed(pat_ty) => Some(pat_ty.pat.to_token_stream().to_string()),
                _ => None,
            })
            .collect();
        assert_eq!(params[0], "hash");
        assert!(params.iter().any(|p| p == "display_name"));

        let new_body = new_fn.block.to_token_stream().to_string();
        assert!(new_body.contains("sk : \"PROFILE\" . to_string"));

        // key_from_hash helper should reuse prefix and const sort key
        let key_helper_body = find_trait_impl(&file.items, "ProfileKey")
            .items
            .iter()
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "from" => Some(func.block.to_token_stream().to_string()),
                _ => None,
            })
            .expect("From impl for key type missing");
        assert!(key_helper_body.contains("concat ! (\"USER#\" , \"{}\")"));
        assert!(key_helper_body.contains("\"PROFILE\" . to_string"));

        // handle_save should enforce the const sort key value
        let entity_impl = find_trait_impl(&file.items, "Profile");
        assert!(has_fn(entity_impl, "handle_save"));
        let handle_save_body = entity_impl
            .items
            .iter()
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "handle_save" => Some(func.block.to_token_stream().to_string()),
                _ => None,
            })
            .expect("handle_save body missing");
        assert!(handle_save_body.contains("self . sk = \"PROFILE\" . to_string"));
    }

    #[test]
    fn sort_key_prefix_adds_begins_with_query_modifier() {
        let file = expand_to_file(quote! {
            struct Order {
                #[hash_key]
                tenant: String,
                #[sort_key(prefix = "ORDER#")]
                sk: String,
                status: String,
            }
        });
        let entity_impl = find_trait_impl(&file.items, "Order");

        let query_body = entity_impl
            .items
            .iter()
            .find_map(|item| match item {
                ImplItem::Fn(func) if func.sig.ident == "handle_query" => {
                    Some(func.block.to_token_stream().to_string())
                }
                _ => None,
            })
            .expect("handle_query function missing");

        assert!(query_body.contains("begins_with"));
        assert!(query_body.contains("\"ORDER#\""));
    }

    #[test]
    fn missing_hash_key_emits_compile_error() {
        let output = expand_raw(quote! {
            struct Broken {
                #[sort_key]
                sk: String,
                value: String,
            }
        });

        assert!(output.contains("compile_error"));
        assert!(output.contains("missing hash key"));
    }

    #[test]
    fn tuple_structs_are_rejected() {
        let output = expand_raw(quote! {
            struct Tuple(String);
        });

        assert!(output.contains("compile_error"));
        assert!(output.contains("requires named fields"));
    }

    #[test]
    fn non_struct_items_are_rejected() {
        let output = expand_raw(quote! {
            enum Sample { A }
        });

        assert!(output.contains("compile_error"));
        assert!(output.contains("can only be derived for structs"));
    }
}
