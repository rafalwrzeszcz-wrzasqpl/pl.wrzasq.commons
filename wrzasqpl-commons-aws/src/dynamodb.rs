/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023, 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use aws_config::{BehaviorVersion, load_defaults};
use aws_sdk_dynamodb::Client;
use aws_sdk_dynamodb::operation::delete_item::DeleteItemError;
use aws_sdk_dynamodb::operation::delete_item::builders::DeleteItemFluentBuilder;
use aws_sdk_dynamodb::operation::get_item::GetItemError;
use aws_sdk_dynamodb::operation::get_item::builders::GetItemFluentBuilder;
use aws_sdk_dynamodb::operation::put_item::PutItemError;
use aws_sdk_dynamodb::operation::put_item::builders::PutItemFluentBuilder;
use aws_sdk_dynamodb::operation::query::builders::QueryFluentBuilder;
use aws_sdk_dynamodb::operation::query::{QueryError, QueryOutput};
use aws_smithy_runtime_api::client::orchestrator::HttpResponse;
use aws_smithy_runtime_api::client::result::SdkError;
use serde::{Deserialize, Serialize};
use serde_dynamo::{Error as SerializationError, from_item, from_items, to_attribute_value, to_item};
use std::env::{VarError, var};
use std::fmt::{Display, Formatter, Result as FormatResult};
use thiserror::Error;
use tracing::{Instrument, Span};
use xray_tracing::aws_metadata;

/// Runtime errors possible for DAO operations.
#[derive(Error, Debug)]
pub enum DaoError {
    MissingConfiguration(#[from] VarError),
    DeleteItemOperation(#[from] SdkError<DeleteItemError, HttpResponse>),
    GetItemOperation(#[from] SdkError<GetItemError, HttpResponse>),
    PutItemOperation(#[from] SdkError<PutItemError, HttpResponse>),
    QueryOperation(#[from] SdkError<QueryError, HttpResponse>),
    Serialization(#[from] SerializationError),
}

impl Display for DaoError {
    fn fmt(&self, formatter: &mut Formatter<'_>) -> FormatResult {
        write!(formatter, "{self:?}")
    }
}

// models

/// Trait describing single entity mapping to DynamoDB item.
pub trait DynamoDbEntity<'serde>: Serialize + Deserialize<'serde> {
    type Key: Serialize + Deserialize<'serde>;

    /// Returns attribute name of the hash key of the primary key.
    fn hash_key_name() -> String;

    /// Returns primary key identifying this entity.
    fn build_key(&self) -> Self::Key;

    /// Hook allowing `PutItem` operation modification.
    ///
    /// It may be used for example to generate emergent properties only needed for indices.
    ///
    /// This hook requires mutable reference.
    ///
    /// Default implementation simply leaves operation unmodified.
    fn handle_save(&mut self, request: PutItemFluentBuilder) -> PutItemFluentBuilder {
        request
    }

    /// Hook allowing `GetItem` operation modification.
    ///
    /// It may be used for example to generate combined keys dynamically.
    ///
    /// Default implementation simply leaves operation unmodified.
    fn handle_load(_key: &Self::Key, request: GetItemFluentBuilder) -> GetItemFluentBuilder {
        request
    }

    /// Hook allowing `DeleteItem` operation modification.
    ///
    /// Default implementation simply leaves operation unmodified.
    fn handle_delete(_key: &Self::Key, request: DeleteItemFluentBuilder) -> DeleteItemFluentBuilder {
        request
    }

    /// Hook allowing `Query` operation modification when executed against main table.
    ///
    /// It may be used for example to add extra filtering.
    ///
    /// Default implementation simply leaves operation unmodified.
    fn handle_query<HashKeyType: Serialize>(
        _hash_key: &HashKeyType,
        request: QueryFluentBuilder,
    ) -> QueryFluentBuilder {
        request
    }

    /// Hook allowing `Query` operation modification when executed against table index.
    ///
    /// It may be used for example to add extra filtering.
    ///
    /// Default implementation simply leaves operation unmodified.
    fn handle_query_index<HashKeyType: Serialize>(
        _index_name: &str,
        _hash_key: &HashKeyType,
        request: QueryFluentBuilder,
    ) -> QueryFluentBuilder {
        request
    }
}

/// Representation of single DynamoDB results page.
pub struct DynamoDbResultsPage<EntityType: Serialize, KeyType: Serialize> {
    /// Current results page.
    pub items: Vec<EntityType>,
    /// Pagination token.
    pub last_evaluated_key: Option<KeyType>,
}

impl<'serde, EntityType: DynamoDbEntity<'serde>, QueryKeyType: Serialize + Deserialize<'serde>> TryFrom<QueryOutput>
    for DynamoDbResultsPage<EntityType, QueryKeyType>
{
    type Error = DaoError;

    fn try_from(results: QueryOutput) -> Result<Self, Self::Error> {
        Ok(Self {
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

// dao

/// DynamoDB DAO object.
///
/// This structure binds DynamoDB table name with current execution context and executes operations scoped to it.
///
/// All operations are typed and requires your entity type to be serializable as all data mapping is handled by
/// `serde_dynamo` crate.
///
/// **Important:** Current implementation is very basic and the API is a subject to change. Plan is to use metadata
/// and have generic struct/trait that will be dedicated to each entity type and bind most of the metadata that now
/// requires manual assignment. Current implementation can handle multiple entity types in single instance, which most
/// likely won't be possible in final shape.
pub struct DynamoDbDao {
    client: Box<Client>,
    table_name: String,
}

impl DynamoDbDao {
    /// Creates new DAO object for given DynamoDB table.
    pub fn new(client: Client, table_name: String) -> Self {
        Self {
            client: Box::new(client),
            table_name,
        }
    }

    /// Creates new DAO object from environment setup.
    pub async fn new_from_env(table_var_name: &str) -> Result<Self, DaoError> {
        let config = load_defaults(BehaviorVersion::v2025_01_17());
        let table_name = var(table_var_name)?;
        let client = Client::new(&config.await);
        Ok(Self::new(client, table_name))
    }

    /// Saves entity in DynamoDB table.
    pub async fn save<'serde, EntityType: DynamoDbEntity<'serde>>(
        &self,
        entity: &mut EntityType,
    ) -> Result<(), DaoError> {
        let item = Some(to_item(&entity)?);

        entity
            .handle_save(self.client.put_item().table_name(&self.table_name).set_item(item))
            .send()
            .instrument(self.instrumentation())
            .await?;

        Ok(())
    }

    /// Loads entity from DynamoDB table. In case there is no matching item, `Ok(None)` is returned.
    pub async fn load<'serde, EntityType: DynamoDbEntity<'serde>>(
        &self,
        key: EntityType::Key,
    ) -> Result<Option<EntityType>, DaoError> {
        Ok(EntityType::handle_load(
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
        .map_or(Ok(None), |entity| entity.map(Some))?)
    }

    /// Deletes entity from DynamoDB table.
    pub async fn delete<'serde, EntityType: DynamoDbEntity<'serde>>(
        &self,
        key: EntityType::Key,
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

    /// Deletes entity from DynamoDB table constructing key based on item reference.
    pub async fn delete_item<'serde, EntityType: DynamoDbEntity<'serde>>(
        &self,
        entity: &EntityType,
    ) -> Result<(), DaoError> {
        self.delete::<EntityType>(entity.build_key()).await
    }

    /// Queries table by given hash key.
    pub async fn query<'serde, EntityType: DynamoDbEntity<'serde>, HashKeyType: Serialize>(
        &self,
        hash_key: HashKeyType,
        page_token: Option<EntityType::Key>,
    ) -> Result<DynamoDbResultsPage<EntityType, EntityType::Key>, DaoError> {
        EntityType::handle_query(
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
        .await?
        .try_into()
    }

    /// Queries table index by hash key.
    pub async fn query_index<
        'serde,
        EntityType: DynamoDbEntity<'serde>,
        HashKeyType: Serialize,
        IndexKeyType: Serialize + Deserialize<'serde>,
    >(
        &self,
        index_name: String,
        hash_key_name: String,
        hash_key: HashKeyType,
        page_token: Option<IndexKeyType>,
    ) -> Result<DynamoDbResultsPage<EntityType, IndexKeyType>, DaoError> {
        EntityType::handle_query_index(
            &index_name,
            &hash_key,
            self.client
                .query()
                .table_name(self.table_name.as_str())
                .index_name(&index_name)
                .key_condition_expression("#attr = :val")
                .expression_attribute_names("#attr", hash_key_name)
                .expression_attribute_values(":val", to_attribute_value(&hash_key)?)
                .set_exclusive_start_key(page_token.map(to_item).map_or(Ok(None), |token| token.map(Some))?),
        )
        .send()
        .instrument(self.instrumentation())
        .await?
        .try_into()
    }

    // will not be public in final version, but for now a lot of functionality may need extra code in consuming projects
    pub fn instrumentation(&self) -> Span {
        aws_metadata(
            self.client.config().region().map(|value| value.to_string()).as_deref(),
            Some(self.table_name.as_str()),
        )
    }
}

#[cfg(test)]
mod tests {
    use crate::{DaoError, DynamoDbDao, DynamoDbEntity, DynamoDbResultsPage};
    use aws_config::{BehaviorVersion, load_defaults};
    use aws_sdk_dynamodb::Client;
    use aws_sdk_dynamodb::config::Builder;
    use aws_sdk_dynamodb::operation::get_item::{GetItemError, GetItemOutput};
    use aws_sdk_dynamodb::operation::put_item::{PutItemError, PutItemOutput};
    use aws_sdk_dynamodb::types::AttributeValue::{L, N, Null, S};
    use aws_sdk_dynamodb::types::{
        AttributeDefinition, BillingMode, GlobalSecondaryIndex, KeySchemaElement, KeyType, Projection, ProjectionType,
        ScalarAttributeType,
    };
    use aws_smithy_runtime_api::client::orchestrator::HttpResponse;
    use aws_smithy_runtime_api::client::result::SdkError;
    use serde::{Deserialize, Serialize};
    use std::env::var;
    use std::future::join;
    use std::sync::atomic::{AtomicUsize, Ordering};
    use test_context::{AsyncTestContext, test_context};
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

    #[derive(Serialize, Deserialize)]
    struct TestEntityIndexKey {
        customer_id: String,
        order_id: String,
        total: u32,
    }

    impl DynamoDbEntity<'_> for TestEntity {
        type Key = TestEntityKey;

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

    impl AsyncTestContext for DynamoDbTestContext {
        async fn setup() -> DynamoDbTestContext {
            let table_name = format!("TestTable{}", NUMBER.fetch_add(1, Ordering::SeqCst));
            let config = load_defaults(BehaviorVersion::v2025_01_17()).await;
            let local_config = Builder::from(&config)
                .endpoint_url(var("DYNAMODB_LOCAL_HOST").unwrap_or("http://localhost:8000".into()))
                .build();
            let client = Client::from_conf(local_config);

            client
                .create_table()
                .table_name(table_name.as_str())
                .attribute_definitions(
                    AttributeDefinition::builder()
                        .attribute_name("customer_id")
                        .attribute_type(ScalarAttributeType::S)
                        .build()
                        .unwrap(),
                )
                .attribute_definitions(
                    AttributeDefinition::builder()
                        .attribute_name("order_id")
                        .attribute_type(ScalarAttributeType::S)
                        .build()
                        .unwrap(),
                )
                .attribute_definitions(
                    AttributeDefinition::builder()
                        .attribute_name("total")
                        .attribute_type(ScalarAttributeType::N)
                        .build()
                        .unwrap(),
                )
                .key_schema(
                    KeySchemaElement::builder()
                        .attribute_name("customer_id")
                        .key_type(KeyType::Hash)
                        .build()
                        .unwrap(),
                )
                .key_schema(
                    KeySchemaElement::builder()
                        .attribute_name("order_id")
                        .key_type(KeyType::Range)
                        .build()
                        .unwrap(),
                )
                .global_secondary_indexes(
                    GlobalSecondaryIndex::builder()
                        .index_name("byTotal")
                        .key_schema(
                            KeySchemaElement::builder()
                                .attribute_name("total")
                                .key_type(KeyType::Hash)
                                .build()
                                .unwrap(),
                        )
                        .projection(Projection::builder().projection_type(ProjectionType::All).build())
                        .build()
                        .unwrap(),
                )
                .billing_mode(BillingMode::PayPerRequest)
                .send()
                .await
                .unwrap();

            let context = DynamoDbTestContext {
                client: Box::new(client.clone()),
                dao: Box::new(DynamoDbDao::new(client, table_name.clone())),
                table_name: table_name.clone(),
            };

            let (res1, res2, res3, res4) = join!(
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
                context.create_record(
                    "ivms.online".into(),
                    "799".into(),
                    100,
                    vec!["water".into(), "sugar".into(), "yast".into(),],
                    Some(2),
                ),
            )
            .await;

            res1.unwrap();
            res2.unwrap();
            res3.unwrap();
            res4.unwrap();

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
        ctx.dao
            .save(&mut TestEntity {
                customer_id: "non-existing".into(),
                order_id: "test0".into(),
                total: 202,
                products: vec![],
                last_update: Some(12),
            })
            .await?;

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
        ctx.dao
            .save(&mut TestEntity {
                customer_id: "wrzasq.pl".into(),
                order_id: "123".into(),
                total: 203,
                products: vec![],
                last_update: Some(13),
            })
            .await?;

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
        ctx.dao
            .delete::<TestEntity>(TestEntityKey {
                customer_id: "wrzasq.pl".into(),
                order_id: "123".into(),
            })
            .await?;

        let result = ctx.load_record("wrzasq.pl".into(), "123".into()).await?;
        assert!(result.item.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn delete_entity_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        ctx.dao
            .delete::<TestEntity>(TestEntityKey {
                customer_id: "non-existing".into(),
                order_id: "test2".into(),
            })
            .await?;

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn delete_entity_entity(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        ctx.dao
            .delete_item(&TestEntity {
                customer_id: "wrzasq.pl".into(),
                order_id: "456".into(),
                total: 0,
                products: vec![],
                last_update: None,
            })
            .await?;

        let result = ctx.load_record("wrzasq.pl".into(), "456".into()).await?;
        assert!(result.item.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityKey> = ctx.dao.query("wrzasq.pl", None).await?;

        assert_eq!(2, page.items.len());
        assert_eq!("123", page.items[0].order_id);

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_page(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityKey> = ctx
            .dao
            .query(
                "wrzasq.pl",
                Some(TestEntityKey {
                    customer_id: "wrzasq.pl".into(),
                    order_id: "123".into(),
                }),
            )
            .await?;

        assert_eq!(1, page.items.len());
        assert_eq!("456", page.items[0].order_id);

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityKey> = ctx.dao.query("non-existing", None).await?;

        assert!(page.items.is_empty());
        assert!(page.last_evaluated_key.is_none());

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_index(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityIndexKey> =
            ctx.dao.query_index("byTotal".into(), "total".into(), 100, None).await?;

        assert_eq!(2, page.items.len());
        assert_eq!("799", page.items[0].order_id);

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_index_page(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityIndexKey> = ctx
            .dao
            .query_index(
                "byTotal".into(),
                "total".into(),
                100,
                Some(TestEntityIndexKey {
                    customer_id: "ivms.online".into(),
                    order_id: "799".into(),
                    total: 100,
                }),
            )
            .await?;

        assert_eq!(1, page.items.len());
        assert_eq!("123", page.items[0].order_id);

        Ok(())
    }

    #[test_context(DynamoDbTestContext)]
    #[tokio_test]
    async fn query_entities_index_unexisting(ctx: &DynamoDbTestContext) -> Result<(), DaoError> {
        let page: DynamoDbResultsPage<TestEntity, TestEntityIndexKey> =
            ctx.dao.query_index("byTotal".into(), "total".into(), 99, None).await?;

        assert!(page.items.is_empty());
        assert!(page.last_evaluated_key.is_none());

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
        ) -> Result<PutItemOutput, SdkError<PutItemError, HttpResponse>> {
            self.client
                .put_item()
                .table_name(self.table_name.as_str())
                .item("customer_id", S(customer_id))
                .item("order_id", S(order_id))
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
        ) -> Result<GetItemOutput, SdkError<GetItemError, HttpResponse>> {
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
