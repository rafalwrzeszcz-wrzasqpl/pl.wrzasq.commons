/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023, 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

#![feature(future_join)]

mod dynamodb;
mod lambda;

pub use crate::dynamodb::{DaoError, DynamoDbDao, DynamoDbEntity, DynamoDbResultsPage};
pub use crate::lambda::{LambdaError, run_lambda};

#[cfg(feature = "derive")]
pub use wrzasqpl_commons_aws_macros::DynamoEntity;

// Reexports used by derive macros and downstream crates
pub mod reexports {
    pub use aws_sdk_dynamodb::operation::put_item::builders::PutItemFluentBuilder;
    pub use aws_sdk_dynamodb::operation::query::builders::QueryFluentBuilder;
    pub use aws_sdk_dynamodb::types::AttributeValue;
}
