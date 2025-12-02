use aws_sdk_dynamodb::Client;
use aws_sdk_dynamodb::config::{
    BehaviorVersion, Builder as DynamoConfigBuilder, Credentials, Region, SharedCredentialsProvider,
};
use aws_sdk_dynamodb::types::{AttributeDefinition, BillingMode, KeySchemaElement, KeyType, ScalarAttributeType};
use aws_smithy_runtime::client::http::hyper_014::HyperClientBuilder;
use hyper::client::HttpConnector;
use std::env::var;
use std::sync::atomic::{AtomicUsize, Ordering};
use tokio::test as tokio_test;
use wrzasqpl_commons_aws::DynamoDbDao;
use wrzasqpl_commons_aws_macros::DynamoEntity;

static NUMBER: AtomicUsize = AtomicUsize::new(0);

#[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
struct PrefixedItem {
    #[hash_key]
    id: String,
    #[sort_key(prefix = "ORD#")]
    sk: String,
    value: i32,
}

#[tokio_test]
async fn query_applies_begins_with_prefix() {
    // Setup client to DynamoDB Local
    let table_name = format!("PrefixedTest{}", NUMBER.fetch_add(1, Ordering::SeqCst));
    let endpoint = match var("DYNAMODB_LOCAL_HOST") {
        Ok(url) => url,
        Err(_) => {
            eprintln!("skipping query_applies_begins_with_prefix: DYNAMODB_LOCAL_HOST not set");
            return;
        }
    };
    let region = var("AWS_REGION").unwrap_or_else(|_| "us-east-1".into());
    let http_connector = HttpConnector::new();
    let http_client = HyperClientBuilder::new().build(http_connector);
    let local_config = DynamoConfigBuilder::new()
        .region(Region::new(region))
        .endpoint_url(endpoint)
        .http_client(http_client)
        .credentials_provider(SharedCredentialsProvider::new(Credentials::for_tests()))
        .behavior_version(BehaviorVersion::latest())
        .build();
    let client = Client::from_conf(local_config);

    // Create table with (id, sk)
    client
        .create_table()
        .table_name(&table_name)
        .attribute_definitions(
            AttributeDefinition::builder()
                .attribute_name("id")
                .attribute_type(ScalarAttributeType::S)
                .build()
                .unwrap(),
        )
        .attribute_definitions(
            AttributeDefinition::builder()
                .attribute_name("sk")
                .attribute_type(ScalarAttributeType::S)
                .build()
                .unwrap(),
        )
        .key_schema(
            KeySchemaElement::builder()
                .attribute_name("id")
                .key_type(KeyType::Hash)
                .build()
                .unwrap(),
        )
        .key_schema(
            KeySchemaElement::builder()
                .attribute_name("sk")
                .key_type(KeyType::Range)
                .build()
                .unwrap(),
        )
        .billing_mode(BillingMode::PayPerRequest)
        .send()
        .await
        .unwrap();

    let dao = DynamoDbDao::new(client.clone(), table_name.clone());

    // Seed data: two matching the prefix and one that shouldn't match
    client
        .put_item()
        .table_name(&table_name)
        .item("id", aws_sdk_dynamodb::types::AttributeValue::S("user1".into()))
        .item("sk", aws_sdk_dynamodb::types::AttributeValue::S("ORD#001".into()))
        .item("value", aws_sdk_dynamodb::types::AttributeValue::N("1".into()))
        .send()
        .await
        .unwrap();

    client
        .put_item()
        .table_name(&table_name)
        .item("id", aws_sdk_dynamodb::types::AttributeValue::S("user1".into()))
        .item("sk", aws_sdk_dynamodb::types::AttributeValue::S("ORD#XYZ".into()))
        .item("value", aws_sdk_dynamodb::types::AttributeValue::N("2".into()))
        .send()
        .await
        .unwrap();

    client
        .put_item()
        .table_name(&table_name)
        .item("id", aws_sdk_dynamodb::types::AttributeValue::S("user1".into()))
        .item("sk", aws_sdk_dynamodb::types::AttributeValue::S("OTHER#777".into()))
        .item("value", aws_sdk_dynamodb::types::AttributeValue::N("3".into()))
        .send()
        .await
        .unwrap();

    // Execute query for hash key using derived entity; begins_with should be applied by macro
    let page = dao.query::<PrefixedItem, _>("user1", None).await.unwrap();
    assert_eq!(page.items.len(), 2);
    assert!(page.items.iter().all(|it| it.sk.starts_with("ORD#")));

    // Cleanup
    client.delete_table().table_name(table_name).send().await.unwrap();
}
