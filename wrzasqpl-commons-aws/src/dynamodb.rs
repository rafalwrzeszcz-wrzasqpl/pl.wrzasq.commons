/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use aws_sdk_dynamodb::operation::delete_item::builders::DeleteItemFluentBuilder;
use aws_sdk_dynamodb::operation::delete_item::DeleteItemError;
use aws_sdk_dynamodb::operation::get_item::builders::GetItemFluentBuilder;
use aws_sdk_dynamodb::operation::get_item::GetItemError;
use aws_sdk_dynamodb::operation::put_item::builders::PutItemFluentBuilder;
use aws_sdk_dynamodb::operation::put_item::PutItemError;
use aws_sdk_dynamodb::operation::query::builders::QueryFluentBuilder;
use aws_sdk_dynamodb::operation::query::{QueryError, QueryOutput};
use aws_sdk_dynamodb::Client;
use aws_smithy_http::result::SdkError;
use serde::{Deserialize, Serialize};
use serde_dynamo::{from_item, from_items, to_attribute_value, to_item, Error as SerializationError};
use std::fmt::{Display, Formatter, Result as FormatResult};
use thiserror::Error;
use tracing::{Instrument, Span};
use xray::aws_metadata;

#[derive(Error, Debug)]
pub enum DaoError {
    DeleteItemOperation(#[from] SdkError<DeleteItemError>),
    GetItemOperation(#[from] SdkError<GetItemError>),
    PutItemOperation(#[from] SdkError<PutItemError>),
    QueryOperation(#[from] SdkError<QueryError>),
    Serialization(#[from] SerializationError),
}

impl Display for DaoError {
    fn fmt(&self, formatter: &mut Formatter<'_>) -> FormatResult {
        write!(formatter, "{self:?}")
    }
}

// models

pub trait DynamoDbEntity<'serde, KeyType: Serialize>: Serialize + Deserialize<'serde> {
    fn hash_key_name() -> String;

    fn build_key(&self) -> KeyType;

    fn handle_save(&self, request: PutItemFluentBuilder) -> PutItemFluentBuilder {
        request
    }

    fn handle_load(_key: &KeyType, request: GetItemFluentBuilder) -> GetItemFluentBuilder {
        request
    }

    fn handle_delete(_key: &KeyType, request: DeleteItemFluentBuilder) -> DeleteItemFluentBuilder {
        request
    }

    fn handle_query<HashKeyType: Serialize>(
        _hash_key: &HashKeyType,
        request: QueryFluentBuilder,
    ) -> QueryFluentBuilder {
        request
    }
}

pub struct DynamoDbResultsPage<EntityType: Serialize, KeyType: Serialize> {
    pub items: Vec<EntityType>,
    pub last_evaluated_key: Option<KeyType>,
}

// dao

pub struct DynamoDbDao {
    client: Box<Client>,
    table_name: String,
}

impl DynamoDbDao {
    pub fn new(client: Client, table_name: String) -> Self {
        Self {
            client: Box::new(client),
            table_name,
        }
    }

    pub async fn save<'serde, KeyType: Serialize, EntityType: DynamoDbEntity<'serde, KeyType>>(
        &self,
        entity: &EntityType,
    ) -> Result<(), DaoError> {
        entity
            .handle_save(
                self.client
                    .put_item()
                    .table_name(&self.table_name)
                    .set_item(Some(to_item(entity)?)),
            )
            .send()
            .instrument(self.instrumentation())
            .await?;

        Ok(())
    }

    pub async fn load<'serde, KeyType: Serialize, EntityType: DynamoDbEntity<'serde, KeyType>>(
        &self,
        key: KeyType,
    ) -> Result<Option<EntityType>, DaoError> {
        EntityType::handle_load(
            &key,
            self.client
                .get_item()
                .table_name(&self.table_name)
                .set_key(Some(to_item(&key)?)),
        )
        .send()
        .instrument(self.instrumentation())
        .await?
        .item
        .map(from_item::<_, EntityType>)
        .map_or(Ok(None), |entity| entity.map(Some))
        .map_err(DaoError::from)
    }

    pub async fn delete<'serde, KeyType: Serialize, EntityType: DynamoDbEntity<'serde, KeyType>>(
        &self,
        key: KeyType,
    ) -> Result<(), DaoError> {
        EntityType::handle_delete(
            &key,
            self.client
                .delete_item()
                .table_name(&self.table_name)
                .set_key(Some(to_item(&key)?)),
        )
        .send()
        .instrument(self.instrumentation())
        .await?;

        Ok(())
    }

    pub async fn delete_item<'serde, KeyType: Serialize, EntityType: DynamoDbEntity<'serde, KeyType>>(
        &self,
        entity: &EntityType,
    ) -> Result<(), DaoError> {
        self.delete::<KeyType, EntityType>(entity.build_key()).await
    }

    pub async fn query<
        'serde,
        KeyType: Serialize + Deserialize<'serde>,
        EntityType: DynamoDbEntity<'serde, KeyType>,
        HashKeyType: Serialize,
    >(
        &self,
        hash_key: HashKeyType,
        page_token: Option<KeyType>,
    ) -> Result<DynamoDbResultsPage<EntityType, KeyType>, DaoError> {
        let results = EntityType::handle_query(
            &hash_key,
            self.client
                .query()
                .table_name(self.table_name.as_str())
                .key_condition_expression("#attr = :val")
                .expression_attribute_names("#attr", EntityType::hash_key_name())
                .expression_attribute_values(":val", to_attribute_value(&hash_key)?)
                .set_exclusive_start_key(page_token.map(to_item).map_or(Ok(None), |token| token.map(Some))?),
        )
        .send()
        .instrument(self.instrumentation())
        .await?;

        DynamoDbDao::build_results_page(results)
    }

    fn instrumentation(&self) -> Span {
        aws_metadata(
            self.client.conf().region().map(|value| value.to_string()).as_deref(),
            Some(self.table_name.as_str()),
        )
    }

    fn build_results_page<
        'serde,
        QueryKeyType: Serialize + Deserialize<'serde>,
        KeyType: Serialize + Deserialize<'serde>,
        EntityType: DynamoDbEntity<'serde, KeyType>,
    >(
        results: QueryOutput,
    ) -> Result<DynamoDbResultsPage<EntityType, QueryKeyType>, DaoError> {
        Ok(DynamoDbResultsPage {
            last_evaluated_key: results
                .last_evaluated_key
                .map(from_item::<_, QueryKeyType>)
                .map_or(Ok(None), |key| key.map(Some))?,
            items: if let Some(items) = results.items {
                from_items(items)?
            } else {
                vec![]
            },
        })
    }
}

#[cfg(test)]
mod tests {
    use crate::{DaoError, DynamoDbDao, DynamoDbEntity, DynamoDbResultsPage};
    use async_trait::async_trait;
    use aws_config::load_from_env;
    use aws_sdk_dynamodb::config::Builder;
    use aws_sdk_dynamodb::operation::delete_item::builders::DeleteItemFluentBuilder;
    use aws_sdk_dynamodb::operation::get_item::builders::GetItemFluentBuilder;
    use aws_sdk_dynamodb::operation::get_item::{GetItemError, GetItemOutput};
    use aws_sdk_dynamodb::operation::put_item::builders::PutItemFluentBuilder;
    use aws_sdk_dynamodb::operation::put_item::{PutItemError, PutItemOutput};
    use aws_sdk_dynamodb::operation::query::builders::QueryFluentBuilder;
    use aws_sdk_dynamodb::types::AttributeValue::{Null, L, N, S};
    use aws_sdk_dynamodb::types::{
        AttributeDefinition, KeySchemaElement, KeyType, ProvisionedThroughput, ScalarAttributeType,
    };
    use aws_sdk_dynamodb::Client;
    use aws_smithy_http::result::SdkError;
    use serde::{Deserialize, Serialize};
    use std::future::join;
    use std::sync::atomic::{AtomicUsize, Ordering};
    use test_context::{test_context, AsyncTestContext};
    use tokio::test as tokio_test;

    static NUMBER: AtomicUsize = AtomicUsize::new(0);

    #[derive(Serialize, Deserialize)]
    struct TestEntity {
        customer_id: String,
        order_id: String,
        total: u32,
        products: Vec<String>,
        last_update: Option<u32>,
    }

    #[derive(Serialize, Deserialize)]
    struct TestEntityKey {
        customer_id: String,
        order_id: String,
    }

    impl DynamoDbEntity<'_, TestEntityKey> for TestEntity {
        fn hash_key_name() -> String {
            "customer_id".into()
        }

        fn build_key(&self) -> TestEntityKey {
            TestEntityKey {
                customer_id: self.customer_id.clone(),
                order_id: self.order_id.clone(),
            }
        }
    }

    struct DynamoDbTestContext {
        client: Box<Client>,
        dao: Box<DynamoDbDao>,
        table_name: String,
    }

    #[async_trait]
    impl AsyncTestContext for DynamoDbTestContext {
        async fn setup() -> DynamoDbTestContext {
            let table_name = format!("TestTable{}", NUMBER.fetch_add(1, Ordering::SeqCst));
            let config = load_from_env().await;
            let local_config = Builder::from(&config).endpoint_url("http://localhost:8000").build();
            let client = Client::from_conf(local_config);

            client
                .create_table()
                .table_name(table_name.as_str())
                .attribute_definitions(
                    AttributeDefinition::builder()
                        .attribute_name("customer_id")
                        .attribute_type(ScalarAttributeType::S)
                        .build(),
                )
                .attribute_definitions(
                    AttributeDefinition::builder()
                        .attribute_name("order_id")
                        .attribute_type(ScalarAttributeType::S)
                        .build(),
                )
                .key_schema(
                    KeySchemaElement::builder()
                        .attribute_name("customer_id")
                        .key_type(KeyType::Hash)
                        .build(),
                )
                .key_schema(
                    KeySchemaElement::builder()
                        .attribute_name("order_id")
                        .key_type(KeyType::Range)
                        .build(),
                )
                .provisioned_throughput(
                    ProvisionedThroughput::builder()
                        .read_capacity_units(1000)
                        .write_capacity_units(1000)
                        .build(),
                )
                .send()
                .await
                .unwrap();

            let context = DynamoDbTestContext {
                client: Box::new(client.clone()),
                dao: Box::new(DynamoDbDao::new(client, table_name.clone())),
                table_name: table_name.clone(),
            };

            let (res1, res2, res3) = join!(
                context.create_record("wrzasq.pl".into(), "123".into(), 100, vec![], None,),
                context.create_record(
                    "wrzasq.pl".into(),
                    "456".into(),
                    200,
                    vec!["flowmeter".into(), "cloud".into(),],
                    Some(2),
                ),
                context.create_record(
                    "ivms.online".into(),
                    "789".into(),
                    768,
                    vec!["flowmeter".into(), "cloud".into(), "support".into(),],
                    Some(2),
                ),
            )
            .await;

            res1.unwrap();
            res2.unwrap();
            res3.unwrap();

            context
        }

        async fn teardown(self) {
            self.client
                .delete_table()
                .table_name(self.table_name)
                .send()
                .await
                .unwrap();
        }
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn create_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let save = ctx
            .dao
            .save(&TestEntity {
                customer_id: "non-existing".into(),
                order_id: "test0".into(),
                total: 202,
                products: vec![],
                last_update: Some(12),
            })
            .await;
        assert!(save.is_ok());

        let record = ctx.load_record("non-existing".into(), "test0".into()).await?;
        assert!(record.item.is_some());

        if let Some(item) = record.item {
            assert_eq!(Ok("12"), item["last_update"].as_n().map(String::as_str));
        }

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn update_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let save = ctx
            .dao
            .save(&TestEntity {
                customer_id: "wrzasq.pl".into(),
                order_id: "123".into(),
                total: 203,
                products: vec![],
                last_update: Some(13),
            })
            .await;
        assert!(save.is_ok());

        let record = ctx.load_record("wrzasq.pl".into(), "123".into()).await?;
        assert!(record.item.is_some());

        if let Some(item) = record.item {
            assert_eq!(Ok("203"), item["total"].as_n().map(String::as_str));
            assert_eq!(Ok("13"), item["last_update"].as_n().map(String::as_str));
        }

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn get_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let result: Option<TestEntity> = ctx
            .dao
            .load(TestEntityKey {
                customer_id: "wrzasq.pl".into(),
                order_id: "123".into(),
            })
            .await?;

        assert!(result.is_some());

        if let Some(entity) = result {
            assert_eq!("wrzasq.pl", entity.customer_id);
            assert_eq!(100, entity.total);
            assert!(entity.products.is_empty());
            assert!(entity.last_update.is_none());
        }

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn get_entity_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let non_existing: Option<TestEntity> = ctx
            .dao
            .load(TestEntityKey {
                customer_id: "non-existing".into(),
                order_id: "test1".into(),
            })
            .await?;
        assert!(non_existing.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn delete_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let result = ctx
            .dao
            .delete::<TestEntityKey, TestEntity>(TestEntityKey {
                customer_id: "wrzasq.pl".into(),
                order_id: "123".into(),
            })
            .await;
        assert!(result.is_ok());

        let result = ctx.load_record("wrzasq.pl".into(), "123".into()).await?;
        assert!(result.item.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn delete_entity_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let result = ctx
            .dao
            .delete::<TestEntityKey, TestEntity>(TestEntityKey {
                customer_id: "non-existing".into(),
                order_id: "test2".into(),
            })
            .await;
        assert!(result.is_ok());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn delete_entity_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let result = ctx
            .dao
            .delete_item(&TestEntity {
                customer_id: "wrzasq.pl".into(),
                order_id: "456".into(),
                total: 0,
                products: vec![],
                last_update: None,
            })
            .await;
        assert!(result.is_ok());

        let result = ctx.load_record("wrzasq.pl".into(), "456".into()).await?;
        assert!(result.item.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let results: Result<DynamoDbResultsPage<TestEntity, TestEntityKey>, DaoError> =
            ctx.dao.query("wrzasq.pl", None).await;
        assert!(results.is_ok());

        if let Ok(page) = results {
            assert_eq!(2, page.items.len());
            assert_eq!("123", page.items[0].order_id);
        }

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_page(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let results: Result<DynamoDbResultsPage<TestEntity, TestEntityKey>, DaoError> = ctx
            .dao
            .query(
                "wrzasq.pl",
                Some(TestEntityKey {
                    customer_id: "wrzasq.pl".into(),
                    order_id: "123".into(),
                }),
            )
            .await;
        assert!(results.is_ok());

        if let Ok(page) = results {
            assert_eq!(1, page.items.len());
            assert_eq!("456", page.items[0].order_id);
        }

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let results: Result<DynamoDbResultsPage<TestEntity, TestEntityKey>, DaoError> =
            ctx.dao.query("non-existing", None).await;
        assert!(results.is_ok());

        if let Ok(page) = results {
            assert!(page.items.is_empty());
            assert!(page.last_evaluated_key.is_none());
        }

        Ok(())
    }

    impl DynamoDbTestContext {
        async fn create_record(
            &self,
            customer_id: String,
            order_id: String,
            total: u32,
            products: Vec<String>,
            last_update: Option<u32>,
        ) -> Result<PutItemOutput, SdkError<PutItemError>> {
            self.client
                .put_item()
                .table_name(self.table_name.as_str())
                .item("customer_id", S(customer_id.to_string()))
                .item("order_id", S(order_id.to_string()))
                .item("total", N(total.to_string()))
                .item("products", L(products.into_iter().map(S).collect()))
                .item(
                    "last_update",
                    last_update.map(|value| N(value.to_string())).unwrap_or(Null(true)),
                )
                .send()
                .await
        }

        async fn load_record(
            &self,
            customer_id: String,
            order_id: String,
        ) -> Result<GetItemOutput, SdkError<GetItemError>> {
            self.client
                .get_item()
                .table_name(self.table_name.as_str())
                .key("customer_id", S(customer_id))
                .key("order_id", S(order_id))
                .send()
                .await
        }
    }
}
