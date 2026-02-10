/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2025 - 2026 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use aws_config::{BehaviorVersion, load_defaults};
use aws_sdk_dynamodb::Client;
use aws_sdk_dynamodb::config::Builder;
use aws_sdk_dynamodb::types::AttributeValue::{N, S};
use aws_sdk_dynamodb::types::{AttributeDefinition, BillingMode, KeySchemaElement, KeyType, ScalarAttributeType};
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
    let config = load_defaults(BehaviorVersion::v2026_01_12()).await;
    let local_config = Builder::from(&config)
        .endpoint_url(var("DYNAMODB_LOCAL_HOST").unwrap_or("http://localhost:8000".into()))
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
        .item("id", S("user1".into()))
        .item("sk", S("ORD#001".into()))
        .item("value", N("1".into()))
        .send()
        .await
        .unwrap();

    client
        .put_item()
        .table_name(&table_name)
        .item("id", S("user1".into()))
        .item("sk", S("ORD#XYZ".into()))
        .item("value", N("2".into()))
        .send()
        .await
        .unwrap();

    client
        .put_item()
        .table_name(&table_name)
        .item("id", S("user1".into()))
        .item("sk", S("OTHER#777".into()))
        .item("value", N("3".into()))
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
