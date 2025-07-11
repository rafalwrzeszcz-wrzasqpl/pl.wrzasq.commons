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
