<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Publishing

You can use `pl.wrzasq.commons.aws.messaging.sqs.QueueClient` to bind target queue URL with a client and avoid passing
excessive information around. `QueueClient` also handles **JSON** serialization of the payload:

```kotlin
data class MyPing(
    val message: String,
    val callback: String
)

class MyProducer(
    private val client: QueueClient
) {
    fun produce() {
        // (2)
        this.client.send(MyPing("hello", "http://localhost/ping"))
    }
}

// (1)
val producer = MyProducer(QueueClient("https://target/"))

class MyLambda {
    companion object {
        @JvmStatic
        fun entryPoint() {
            // (3)
            producer.produce()
        }
    }
}
```

1.  You can initialize `QueueClient` with custom **Jackson** `ObjectMapper` to handle your custom types/strategies.
1.  Note that you don't need to pass queue URL around - it's wrapped in the client.
1.  See that your logic has now no call-time parameters, it's all covered by underlying client.

# Handling messages

If you want your **Lambda** to process [**SQS**](https://aws.amazon.com/sqs/) messages you can use
`pl.wrzasq.commons.aws.sqs.messaging.EventHandler` class to implement your function as an event source. Flow here is
similar to handling **SNS** notifications. In such flow, your **Lambda** is called with an SQS event as an argument:

```kotlin
val handler = SimpleEventHandler { println(it) }

class MyLambda {
    companion object {
        @JvmStatic
        fun entryPoint(event: SQSEvent) {
            handler.process(event)
        }
    }
}
```

**Note:** Single SQS event can contain multiple messages - your handler will be called for each of the messages.

You can also use typed handler:

```kotlin
data class MyPojo(
    val name: String,
    val email: String
)

class MyConsumer {
    fun consume(payload: MyPojo) {
        // (1)
    }
}

val objectMapper = ObjectMapper()
// configure your object mapper

val consumer = MyConsumer()
// configure your consumer

// (2)
val handler = TypedEventHandler(
    objectMapper,
    consumer::consume,
    MyPojo::class.java
)

class MyLambda {
    companion object {
        @JvmStatic
        fun entryPoint(event: SQSEvent) {
            handler.process(event)
        }
    }
}
```
