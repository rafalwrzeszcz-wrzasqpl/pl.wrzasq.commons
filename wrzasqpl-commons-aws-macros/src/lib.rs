/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

//! Proc-macro helpers for DynamoDB entities used by `wrzasqpl-commons-aws`.
//!
//! This crate exposes the `DynamoEntity` derive macro which generates
//! implementations and helpers needed to work with the `DynamoDbEntity` trait
//! from the `wrzasqpl-commons-aws` crate.
//!
//! Quick start
//! - Put `#[derive(DynamoEntity)]` on a struct that models a DynamoDB item.
//! - Mark the hash key and sort key fields, or rely on defaults:
//!   - Hash key: field marked with `#[hash_key]`, or a field named `id`.
//!   - Sort key: field marked with `#[sort_key]`, or a field named `sk`.
//! - Optional attribute options:
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
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
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
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
//! struct Invoice {
//!     id: String, // used as hash key by default
//!     sk: String, // used as sort key by default
//!     amount_cents: i64,
//! }
//! ```
//!
//! Constant sort key and hash prefix
//! ```
//! use wrzasqpl_commons_aws_macros::DynamoEntity;
//!
//! #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
//! struct Profile {
//!     #[hash_key(prefix = "USER#")]
//!     id: String,
//!     #[sort_key(const = "PROFILE")]
//!     sk: String, // value enforced to "PROFILE"
//!     display_name: String,
//! }
//!
//! // With const sort key, the macro provides:
//! // - Profile::new(hash, display_name)
//! // - Profile::key_from_hash(hash) -> ProfileKey
//! ```
//!
use proc_macro::TokenStream;
use proc_macro2::Span;
use quote::{format_ident, quote};
use syn::{parse_macro_input, spanned::Spanned, DeriveInput, Meta};

#[proc_macro_derive(DynamoEntity, attributes(hash_key, sort_key))]
/// Derive macro that turns a struct into a DynamoDB entity compatible with
/// `wrzasqpl-commons-aws`.
///
/// Keys
/// - Hash key: mark a field with `#[hash_key]` or provide a field named `id`.
/// - Sort key: mark a field with `#[sort_key]` or provide a field named `sk`.
///
/// Options
/// - `#[hash_key(prefix = "...")]` – prefixes hash values on constructors.
/// - `#[sort_key(const = "...")]` – uses a constant sort key value.
/// - `#[sort_key(prefix = "...")]` – queries apply `begins_with` on that prefix.
///
/// Examples
/// ```
/// use wrzasqpl_commons_aws_macros::DynamoEntity;
///
/// #[derive(DynamoEntity)]
/// struct Example { id: String, sk: String }
/// ```
pub fn derive_dynamo_entity(input: TokenStream) -> TokenStream {
    let input = parse_macro_input!(input as DeriveInput);

    let struct_ident = input.ident.clone();
    let input_span = input.span();

    // Only support named-field structs
    let data_struct = match input.data {
        syn::Data::Struct(ds) => ds,
        _ => {
            return syn::Error::new(input_span, "DynamoEntity can only be derived for structs")
                .to_compile_error()
                .into()
        }
    };

    let fields = match data_struct.fields {
        syn::Fields::Named(named) => named.named,
        _ => {
            return syn::Error::new(input_span, "DynamoEntity requires named fields (struct with field names)")
                .to_compile_error()
                .into()
        }
    };

    // Discover hash_key and sort_key fields
    struct KeyField {
        ident: syn::Ident,
        ty: syn::Type,
        const_value: Option<String>,
        prefix: Option<String>,
    }

    let mut hash_key: Option<KeyField> = None;
    let mut sort_key: Option<KeyField> = None;

    for f in fields.iter() {
        let Some(ident) = f.ident.clone() else { continue };
        let mut is_hash = false;
        let mut is_sort = false;
        let mut const_value: Option<String> = None;
        let mut prefix: Option<String> = None;

        for attr in &f.attrs {
            let path = attr.path();
            if path.is_ident("hash_key") {
                is_hash = true;
                // Parse optional meta: hash_key(prefix = "...")
                match &attr.meta {
                    Meta::Path(_) => {}
                    Meta::List(list) => {
                        let _ = list.parse_nested_meta(|nested| {
                            if nested.path.is_ident("prefix") {
                                let s: syn::LitStr = nested.value()?.parse()?;
                                prefix = Some(s.value());
                            }
                            Ok(())
                        });
                    }
                    Meta::NameValue(_) => {}
                }
            }
            if path.is_ident("sort_key") {
                is_sort = true;
                // Parse optional meta: sort_key(const = "...", prefix = "...")
                match &attr.meta {
                    Meta::Path(_) => {}
                    Meta::List(list) => {
                        let _ = list.parse_nested_meta(|nested| {
                            if nested.path.is_ident("const") {
                                let s: syn::LitStr = nested.value()?.parse()?;
                                const_value = Some(s.value());
                            } else if nested.path.is_ident("prefix") {
                                let s: syn::LitStr = nested.value()?.parse()?;
                                prefix = Some(s.value());
                            }
                            Ok(())
                        });
                    }
                    Meta::NameValue(_) => {}
                }
            }
        }

        if is_hash || (!is_sort && ident == "id") {
            // Default to field named "id" if not explicitly marked and no explicit hash_key found yet
            if hash_key.is_none() || is_hash {
                hash_key = Some(KeyField { ident: ident.clone(), ty: f.ty.clone(), const_value: None, prefix: None });
                if is_hash { continue; }
            }
        }
        if is_sort || ident == "sk" {
            // Default to field named "sk" if not explicitly marked and no explicit sort_key found yet
            if sort_key.is_none() || is_sort {
                sort_key = Some(KeyField { ident: ident.clone(), ty: f.ty.clone(), const_value, prefix });
            }
        }
    }

    let Some(hk) = hash_key else {
        return syn::Error::new(
            input_span,
            "DynamoEntity: missing hash key. Mark a field with #[hash_key] or include a field named 'id'.",
        )
        .to_compile_error()
        .into();
    };
    let Some(sk) = sort_key else {
        return syn::Error::new(
            input_span,
            "DynamoEntity: missing sort key. Mark a field with #[sort_key] or include a field named 'sk', or add #[sort_key(const = \"...\")] to the struct.",
        )
        .to_compile_error()
        .into();
    };

    // Names and idents
    let hk_ident = hk.ident;
    let hk_ty = hk.ty;
    let hk_prefix = hk.prefix;
    let hk_name_str = syn::LitStr::new(&hk_ident.to_string(), Span::call_site());

    let sk_ident = sk.ident;
    let sk_ty = sk.ty;
    let sk_prefix = sk.prefix;
    let sk_const = sk.const_value;
    let sk_name_str = syn::LitStr::new(&sk_ident.to_string(), Span::call_site());

    let key_ident = format_ident!("{}Key", struct_ident);

    // Non-key fields list for constructors based on parsed fields
    let non_key_fields: Vec<(syn::Ident, syn::Type)> = fields
        .iter()
        .filter_map(|f| {
            let ident = f.ident.clone()?;
            if ident == hk_ident || ident == sk_ident { None } else { Some((ident, f.ty.clone())) }
        })
        .collect();

    let sk_field_exists = fields
        .iter()
        .any(|f| f.ident.as_ref().map(|i| i == &sk_ident).unwrap_or(false));

    // sk expression (const or field)
    let sk_expr = if let Some(c) = &sk_const {
        let lit = syn::LitStr::new(c, Span::call_site());
        quote! { #lit.to_string() }
    } else {
        quote! { self.#sk_ident.clone() }
    };

    // Implement handle_query to add begins_with for optional sk prefix
    let handle_query_impl = if let Some(prefix) = sk_prefix {
        let lit = syn::LitStr::new(prefix.as_str(), Span::call_site());
        let sk_name_str = sk_name_str.clone();
        quote! {
            fn handle_query<HashKeyType: serde::Serialize>(
                _hash_key: &HashKeyType,
                request: ::wrzasqpl_commons_aws::reexports::QueryFluentBuilder,
            ) -> ::wrzasqpl_commons_aws::reexports::QueryFluentBuilder {
                use ::wrzasqpl_commons_aws::reexports::AttributeValue as __DdbAttrVal;
                request
                    .key_condition_expression("#attr = :val AND begins_with(#sk, :sk)")
                    .expression_attribute_names("#sk", #sk_name_str)
                    .expression_attribute_values(":sk", __DdbAttrVal::S(#lit.to_string()))
            }
        }
    } else {
        quote! {}
    };

    // Optional inherent impl constructor for keys when sk is const
    let key_from_hash_impl = if let Some(sk_c) = &sk_const {
        let sk_lit = syn::LitStr::new(&sk_c, Span::call_site());
        if let Some(hp) = &hk_prefix {
            let hp_lit = syn::LitStr::new(&hp, Span::call_site());
            quote! {
                impl #struct_ident {
                    pub fn key_from_hash(hash: impl Into<::std::string::String>) -> #key_ident {
                        let __h: ::std::string::String = hash.into();
                        #key_ident {
                            #hk_ident: format!(concat!(#hp_lit, "{}"), __h),
                            #sk_ident: #sk_lit.to_string(),
                        }
                    }
                }
            }
        } else {
            quote! {
                impl #struct_ident {
                    pub fn key_from_hash(hash: impl Into<::std::string::String>) -> #key_ident {
                        #key_ident {
                            #hk_ident: hash.into(),
                            #sk_ident: #sk_lit.to_string(),
                        }
                    }
                }
            }
        }
    } else { quote! {} };

    // Inherent impl: generic constructor `new(...)` to build an entity from hash (+ sk if needed) and non-key fields
    let new_constructor_impl = {
        // Build param list for non-key fields
        let (nk_idents, nk_tys):(Vec<syn::Ident>, Vec<syn::Type>) = non_key_fields.iter().cloned().unzip();
        if let Some(sk_c) = &sk_const {
            // sk is constant, omit from params
            if let Some(hp) = &hk_prefix {
                let hp_lit = syn::LitStr::new(&hp, Span::call_site());
                let sk_lit = syn::LitStr::new(&sk_c, Span::call_site());
                let sk_init = if sk_field_exists { quote! { #sk_ident: #sk_lit.to_string(), } } else { quote! {} };
                quote! {
                    impl #struct_ident {
                        pub fn new<Hash: Into<::std::string::String>>(hash: Hash, #( #nk_idents: #nk_tys ),* ) -> Self {
                            let __h: ::std::string::String = hash.into();
                            Self {
                                #hk_ident: format!(concat!(#hp_lit, "{}"), __h),
                                #sk_init
                                #( #nk_idents: #nk_idents ),*
                            }
                        }
                    }
                }
            } else {
                let sk_lit = syn::LitStr::new(&sk_c, Span::call_site());
                let sk_init = if sk_field_exists { quote! { #sk_ident: #sk_lit.to_string(), } } else { quote! {} };
                quote! {
                    impl #struct_ident {
                        pub fn new<Hash: Into<::std::string::String>>(hash: Hash, #( #nk_idents: #nk_tys ),* ) -> Self {
                            Self { #hk_ident: hash.into(), #sk_init #( #nk_idents: #nk_idents ),* }
                        }
                    }
                }
            }
        } else {
            // Require sort param if not constant
            if let Some(hp) = &hk_prefix {
                let hp_lit = syn::LitStr::new(&hp, Span::call_site());
                quote! {
                    impl #struct_ident {
                        pub fn new<Hash: Into<::std::string::String>, Sort: Into<::std::string::String>>(hash: Hash, sort: Sort, #( #nk_idents: #nk_tys ),* ) -> Self {
                            let __h: ::std::string::String = hash.into();
                            Self { #hk_ident: format!(concat!(#hp_lit, "{}"), __h), #sk_ident: sort.into(), #( #nk_idents: #nk_idents ),* }
                        }
                    }
                }
            } else {
                quote! {
                    impl #struct_ident {
                        pub fn new<Hash: Into<::std::string::String>, Sort: Into<::std::string::String>>(hash: Hash, sort: Sort, #( #nk_idents: #nk_tys ),* ) -> Self {
                            Self { #hk_ident: hash.into(), #sk_ident: sort.into(), #( #nk_idents: #nk_idents ),* }
                        }
                    }
                }
            }
        }
    };

    // When sort key is const and the struct contains the sk field, enforce it on save
    let handle_save_const_impl = if sk_const.is_some() && sk_field_exists {
        let sk_lit = syn::LitStr::new(sk_const.as_ref().unwrap(), Span::call_site());
        quote! {
            fn handle_save(
                &mut self,
                request: ::wrzasqpl_commons_aws::reexports::PutItemFluentBuilder,
            ) -> ::wrzasqpl_commons_aws::reexports::PutItemFluentBuilder {
                self.#sk_ident = #sk_lit.to_string();
                request
            }
        }
    } else { quote! {} };

    let expanded = quote! {
        #[allow(non_camel_case_types)]
        #[derive(serde::Serialize, serde::Deserialize, Debug, Clone)]
        pub struct #key_ident {
            pub #hk_ident: #hk_ty,
            pub #sk_ident: #sk_ty,
        }

        impl ::wrzasqpl_commons_aws::DynamoDbEntity<'_> for #struct_ident {
            type Key = #key_ident;
            fn hash_key_name() -> ::std::string::String { #hk_name_str.to_string() }
            fn build_key(&self) -> Self::Key { Self::Key { #hk_ident: self.#hk_ident.clone(), #sk_ident: #sk_expr } }
            #handle_query_impl
            #handle_save_const_impl
        }

        #key_from_hash_impl
        #new_constructor_impl
    };

    TokenStream::from(expanded)
}
