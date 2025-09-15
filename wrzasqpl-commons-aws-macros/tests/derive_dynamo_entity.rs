use wrzasqpl_commons_aws::DynamoDbEntity;
use wrzasqpl_commons_aws_macros::DynamoEntity;

#[test]
fn derives_with_default_keys() {
    #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
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
    let o = Order::new("A", "B", "processing".to_string(), 5);
    assert_eq!(o.id, "A");
    assert_eq!(o.sk, "B");
    assert_eq!(o.status, "processing");
    assert_eq!(o.count, 5);

    // build_key should reflect instance values when sort key is not const
    let key = o.build_key();
    assert_eq!(key.id, "A");
    assert_eq!(key.sk, "B");
}

#[test]
fn derives_with_explicit_key_attributes() {
    #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
    struct Record {
        #[hash_key]
        customer_id: String,
        #[sort_key]
        order_id: String,
        amount_cents: i64,
    }

    // explicit hash key name
    assert_eq!(Record::hash_key_name(), "customer_id");

    let r = Record::new("cust-1", "ord-9", 1234);
    assert_eq!(r.customer_id, "cust-1");
    assert_eq!(r.order_id, "ord-9");
    assert_eq!(r.amount_cents, 1234);

    let key = r.build_key();
    assert_eq!(key.customer_id, "cust-1");
    assert_eq!(key.order_id, "ord-9");
}

#[test]
fn const_sort_key_and_hash_prefix() {
    #[derive(DynamoEntity, serde::Serialize, serde::Deserialize, Clone, Debug)]
    struct Profile {
        #[hash_key(prefix = "USER#")]
        id: String,
        #[sort_key(const = "PROFILE")]
        sk: String,
        display_name: String,
    }

    // With const sort key, new(hash, other..) applies const to field and prefix to hash
    let p = Profile::new("123", "Rafal".to_string());
    assert_eq!(p.id, "USER#123");
    assert_eq!(p.sk, "PROFILE");
    assert_eq!(p.display_name, "Rafal");

    // key_from_hash helper should use the same prefix + const sort key
    let key = Profile::key_from_hash("456");
    assert_eq!(key.id, "USER#456");
    assert_eq!(key.sk, "PROFILE");

    // build_key returns const sort key value regardless of field contents
    let mut p2 = Profile {
        id: "USER#zzz".into(),
        sk: "WRONG".into(),
        display_name: "X".into(),
    };
    let key2 = p2.build_key();
    assert_eq!(key2.sk, "PROFILE");
}
