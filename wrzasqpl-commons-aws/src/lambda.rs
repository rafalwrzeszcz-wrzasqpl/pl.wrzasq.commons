/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use env_logger::Builder;
use lambda_runtime::{run, service_fn, Error, LambdaEvent};
use serde::{Deserialize, Serialize};
use std::fmt::{Debug, Display};
use std::future::Future;
use tracing_core::dispatcher::set_global_default;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::Registry;
use xray_tracing::XRaySubscriber;

/// Runs a Lambda handler as a service.
///
/// This function rust a specified Lambda handler after setting up common environment, that means logging and tracing
/// with AWS X-Ray.
///
/// # Examples
///
/// ```
/// use lambda_runtime::{Error, LambdaEvent};
/// use serde::Deserialize;
/// use tokio::main as tokio_main;
/// use wrzasqpl_commons_aws::run_lambda;
///
/// #[derive(Deserialize)]
/// struct Request {
///     customer_id: String,
///     order_id: String,
/// }
///
/// #[tokio_main]
/// async fn main() -> Result<(), Error> {
///     let dao = &DynamoDbDao::load_from_env().await?; // be your own db handler
///
///     run_lambda(move |event: LambdaEvent<Request>| async move {
///         dao.delete_item(event.payload.customer_id, event.payload.order_id).await
///     }).await
/// }
/// ```
pub async fn run_lambda<PayloadType, HandlerType, FutureType, ReturnType, ErrorType>(
    func: HandlerType,
) -> Result<(), Error>
where
    PayloadType: for<'serde> Deserialize<'serde>,
    HandlerType: Fn(LambdaEvent<PayloadType>) -> FutureType,
    FutureType: Future<Output = Result<ReturnType, ErrorType>>,
    ReturnType: Serialize,
    ErrorType: Debug + Display,
{
    Builder::from_default_env().format_timestamp(None).init();

    set_global_default(Registry::default().with(XRaySubscriber::default()).into())?;

    run(service_fn(func)).await
}

/// Convenient shorthand for running async handler.
///
/// By using macro you don't need to `.await` run_lambda() call.
#[macro_export]
macro_rules! run_lambda {
    ($handler:expr) => {
        run_lambda($handler).await
    };
}
