/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use env_logger::Builder;
use lambda_runtime::{run, service_fn, Diagnostic, Error as LambdaRuntimeError, LambdaEvent};
use serde::{Deserialize, Serialize};
use std::env::VarError;
use std::fmt::{Debug, Display, Formatter, Result as FormatResult};
use std::future::Future;
use thiserror::Error;
use tracing_core::dispatcher::set_global_default;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::Registry;
use xray_tracing::XRaySubscriber;

/// Runtime errors possible for Lambda operations.
#[derive(Error, Debug)]
pub enum LambdaError {
    MissingConfiguration(#[from] VarError),
    NoHandler(String),
}

impl Display for LambdaError {
    fn fmt(&self, formatter: &mut Formatter<'_>) -> FormatResult {
        write!(formatter, "{self:?}")
    }
}

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
/// use wrzasqpl_commons_aws::{run_lambda, DynamoDbDao};
///
/// #[derive(Deserialize)]
/// struct Request {
///     customer_id: String,
///     order_id: String,
/// }
///
/// #[tokio_main]
/// async fn main() -> Result<(), Error> {
///     let dao = &DynamoDbDao::load_from_env().await?;
///
///     run_lambda(move |event: LambdaEvent<Request>| async move {
///         dao.delete((event.payload.customer_id, event.payload.order_id).into()).await
///     }).await
/// }
/// ```
pub async fn run_lambda<PayloadType, HandlerType, FutureType, ReturnType, ErrorType>(
    func: HandlerType,
) -> Result<(), LambdaRuntimeError>
where
    PayloadType: for<'serde> Deserialize<'serde>,
    HandlerType: Fn(LambdaEvent<PayloadType>) -> FutureType,
    FutureType: Future<Output = Result<ReturnType, ErrorType>>,
    ReturnType: Serialize,
    ErrorType: Into<Diagnostic> + Debug + Display,
{
    Builder::from_default_env().format_timestamp(None).init();

    set_global_default(Registry::default().with(XRaySubscriber::default()).into())?;

    run(service_fn(func)).await
}

/// Convenient shorthand for running async handler.
///
/// By using macro you don't need to `.await` run_lambda() call.
///
/// It can also accept `key: value,` pairs to provide branches for multiple handlers - this version
/// of macro will use `_HANDLER` environment variable to decide which handler to execute:
///
/// ```
/// use lambda_runtime::Error;
/// use tokio::main as tokio_main;
/// use wrzasqpl_commons_aws::run_lambda;
///
/// #[tokio_main]
/// async fn main() -> Result<(), Error> {
///     run_lambda!(
///         "customer:create": create_customer(IamFacade::load_from_env().await?),
///         "customer:delete": delete_customer(IamFacade::load_from_env().await?),
///         "customer:fetch": fetch_customer(new_from_env().await?),
///     )
/// }
/// ```
#[macro_export]
macro_rules! run_lambda {
    ($handler:expr) => {
        run_lambda($handler).await
    };

    ($($key:tt: $val:expr),* $(,)?) => {{
        match var("_HANDLER")?.as_str() {
        $(
            $key => run_lambda($val).await,
        )*
            missing => Err(LambdaError::NoHandler(missing.to_string()).into()),
        }
    }};
}
