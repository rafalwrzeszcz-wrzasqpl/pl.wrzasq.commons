/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

use serde::{Deserialize, Serialize};
use uuid::{Uuid, uuid};
use wrzasqpl_commons_aws::DynamoDbEntity;
use wrzasqpl_commons_aws_macros::DynamoEntity;

const TEST_CUSTOMER_ID: Uuid = uuid!("00000000-0000-0000-0000-000000000000");

#[test]
fn derive_with_hash_key_only() {
    #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
    #[key_attrs(serde(rename_all = "camelCase"))]
    struct Customer {
        #[hash_key(name = "customerId")]
        customer_id: Uuid,
        name: String,
    }

    // hash_key_name should be the default field name
    assert_eq!(Customer::hash_key_name(), "customerId");

    // constructor with hash + extra fields
    let customer = Customer::new(TEST_CUSTOMER_ID, "John Doe".to_string());

    // build_key should reflect instance values
    let key = customer.build_key();
    assert_eq!(key.customer_id, TEST_CUSTOMER_ID);
}

#[test]
fn derive_with_hash_key_only_produces_from_impl() {
    #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
    struct Customer {
        #[hash_key(name = "customerId")]
        customer_id: Uuid,
        name: String,
    }

    Customer::new(TEST_CUSTOMER_ID, "John Doe".to_string());

    // build_key should reflect instance values
    let key = CustomerKey::from(TEST_CUSTOMER_ID);
    assert_eq!(key.customer_id, TEST_CUSTOMER_ID);
}

#[test]
fn derives_with_default_keys() {
    #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
    struct Order {
        // defaults: id -> hash key, sk -> sort key
        id: String,
        sk: String,
        status: String,
        count: u32,
    }

    // hash_key_name should be the default field name
    assert_eq!(Order::hash_key_name(), "id");

    // constructor with hash + sort + extra fields
    let order = Order::new("A".into(), "B".into(), "processing".to_string(), 5);
    assert_eq!(order.id, "A");
    assert_eq!(order.sk, "B");
    assert_eq!(order.status, "processing");
    assert_eq!(order.count, 5);

    // build_key should reflect instance values when sort key is not const
    let key = order.build_key();
    assert_eq!(key.id, "A");
    assert_eq!(key.sk, "B");
}

#[test]
fn derives_with_explicit_key_attributes() {
    #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
    struct Record {
        #[hash_key]
        customer_id: String,
        #[sort_key]
        order_id: String,
        amount_cents: i64,
    }

    // explicit hash key name
    assert_eq!(Record::hash_key_name(), "customer_id");

    let record = Record::new("cust-1".into(), "ord-9".into(), 1234);
    assert_eq!(record.customer_id, "cust-1");
    assert_eq!(record.order_id, "ord-9");
    assert_eq!(record.amount_cents, 1234);

    let key = record.build_key();
    assert_eq!(key.customer_id, "cust-1");
    assert_eq!(key.order_id, "ord-9");
}

#[test]
fn const_sort_key_and_hash_prefix() {
    #[derive(DynamoEntity, Serialize, Deserialize, Clone, Debug)]
    struct Profile {
        #[hash_key(prefix = "USER#")]
        id: String,
        #[sort_key(const = "PROFILE")]
        sk: String,
        display_name: String,
    }

    // With const sort key, new(hash, other..) applies const to field and prefix to hash
    let profile1 = Profile::new("123".into(), "Rafal".to_string());
    assert_eq!(profile1.id, "USER#123");
    assert_eq!(profile1.sk, "PROFILE");
    assert_eq!(profile1.display_name, "Rafal");

    // key_from_hash helper should use the same prefix + const sort key
    let key1 = ProfileKey::from("456".to_string());
    assert_eq!(key1.id, "USER#456");
    assert_eq!(key1.sk, "PROFILE");

    // build_key returns const sort key value regardless of field contents
    let profile2 = Profile {
        id: "USER#zzz".into(),
        sk: "WRONG".into(),
        display_name: "X".into(),
    };
    let key2 = profile2.build_key();
    assert_eq!(key2.sk, "PROFILE");
}
