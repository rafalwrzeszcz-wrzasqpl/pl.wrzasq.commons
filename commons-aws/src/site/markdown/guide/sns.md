<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Publishing

When you deal with [**SNS**](https://aws.amazon.com/sns/) you most likely transfer messages with the form of **JSON**.
Usually you also work against specified SNS topic (which means it's ARN won't change).
`pl.wrzasq.commons.aws.sns.messaging.TopicClient` is a simple wrapper that wraps single topic communication and handles
**JSON** conversion:

```kotlin
data class MyPing(
    val message: String,
    val callback: String
)

class MyProducer(
    private val client: TopicClient
) {
    fun produce() {
        // (2)
        this.client.send(MyPing("hello", "http://localhost/ping"))
    }
}

// (1)
val producer = MyProducer(TopicClient("arn:test"))

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

1.  You can initialize `TopicClient` with custom **Jackson** `ObjectMapper` to handle your custom types/strategies.
1.  Note that you don't need to pass topic ARN around - it's wrapped in the client.
1.  See that your logic has now no call-time parameters, it's all covered by underlying client.

# Handling events

To process incoming events you can use `pl.wrzasq.commons.aws.sns.messaging.NotificationHandler` class - it extracts
**SNS** records from the event and invokes message handler for each of them:

```kotlin
class MyConsumer {
    fun consume(message: SNSEvent.SNS) {
        // handle single message
    }
}

val consumer = MyConsumer()

// (1)
val handler = NotificationHandler(consumer::consume)

class MyLambda {
    companion object {
        @JvmStatic
        fun entryPoint(event: SNSEvent) {
            // (2)
            handler.process(event)
        }
    }
}
```

1.  All you expose to the handler is single message consumer method, you keep your components separated.
1.  Your **Lambda** entry point gets lean.

## Simple notifications handling

If you are only interested in notification body handling, you can use even simpler wrapper `SimpleNotificationHandler`,
which also extracts message body, giving you just it's content:

```kotlin
val handler = SimpleNotificationHandler { println(it) }

class MyLambda {
    companion object {
        @JvmStatic
        fun entryPoint(event: SNSEvent) {
            handler.process(event)
        }
    }
}
```

## Typed messages

This is still not all - you can also define typed message consumer that will automatically handle your **JSON**
handling:

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
val handler = TypedNotificationHandler(
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

1.  In your logic you get your deserialized data model.
1.  All you need to do is to plug your entry point.
