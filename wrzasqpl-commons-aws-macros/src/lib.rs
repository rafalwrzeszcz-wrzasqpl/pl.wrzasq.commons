/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

mod dynamodb;

use crate::dynamodb::derive_dynamo_entity_impl;

use proc_macro::TokenStream;
use syn::{DeriveInput, parse_macro_input};

#[proc_macro_derive(DynamoEntity, attributes(hash_key, sort_key, key_attrs))]
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
/// struct Example {
///     id: String,
///     sk: String,
/// }
/// ```
pub fn derive_dynamo_entity(input: TokenStream) -> TokenStream {
    let input = parse_macro_input!(input as DeriveInput);
    derive_dynamo_entity_impl(input).into()
}
