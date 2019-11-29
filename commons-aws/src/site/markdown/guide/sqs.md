<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Handling messages

If you want your **Lambda** to process [**SQS**](https://aws.amazon.com/sqs/) messages you can use `pl.wrzasq.commons.aws.sqs.QueueHandler` class. It takes care for iterating over fetched messages and and deletes processed messages from the queue:

```java
class MyConsumer
{
    public void consume(Message message)
    {
        // handle single message
    }
}

public class MyLambda
{
    private static QueueHandler handler;

    static {
        MyConsumer consumer = new MyConsumer();

        // (1)
        MyLambda.handler = new QueueHandler(
            System.getenv("MY_QUEUE_URL"),
            consumer::consume
        );
    }

    public static void entryPoint()
    {
        // (2)
        MyLambda.handler.process();
    }
}
```

1.  All you expose to the handler is single message consumer method, you keep your components separated.
1.  Your **Lambda** entry point gets lean.

## Simple messages handling

If you are only interested in message body handling, you can use even simpler wrapper `SimpleMessageHandler`, which also extracts message body, giving you just it's content:

```java
public class MyLambda
{
    private static QueueHandler handler;

    static {
        // all you have is plain string
        MyLambda.handler = new SimpleQueueHandler(
            System.getenv("MY_QUEUE_URL"),
            (String content) -> System.out.println(content)
        );
    }

    public static void entryPoint()
    {
        MyLambda.handler.process();
    }
}
```

## Typed messages

If you work with serialized structures, you can also automate this by using `TypedQueueHandler` which deserializes **JSON** for you:

```java
class MyPojo
{
    public String name;
    public String email;
}

class MyConsumer
{
    public void consume(MyPojo payload)
    {
        // (1)
    }
}

public class MyLambda
{
    private static QueueHandler handler;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        // configure your object mapper

        MyConsumer consumer = new MyConsumer();
        // configure your consumer

        // (2)
        MyLambda.handler = new TypedQueueHandler(
            System.getenv("MY_QUEUE_URL"),
            objectMapper,
            consumer::consume,
            MyPojo.class
        );
    }

    public static void entryPoint()
    {
        MyLambda.handler.process();
    }
}
```

1.  In your logic you get your unserialized data model.
1.  All you need to do is to plug your entry point.

# Event handling

It's also possible to use **SQS** as an event source to build reactive **Lambda** functions. Flow here is similar to
handling **SNS** notifications. In such flow, your **Lambda** is called with an SQS event as an argument:

```java
public class MyLambda
{
    private static EventHandler handler;

    static {
        // all you have is plain string
        MyLambda.handler = new SimpleEventHandler(
            (String content) -> System.out.println(content)
        );
    }

    public static void entryPoint(SQSEvent event)
    {
        MyLambda.handler.process(event);
    }
}
```

**Note:** Single SQS event can contain multiple messages - your handler will be called for each of the messages.

You can also use typed handler:

```java
class MyPojo
{
    public String name;
    public String email;
}

class MyConsumer
{
    public void consume(MyPojo payload)
    {
        // (1)
    }
}

public class MyLambda
{
    private static EventHandler handler;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        // configure your object mapper

        MyConsumer consumer = new MyConsumer();
        // configure your consumer

        // (2)
        MyLambda.handler = new TypedEventHandler(
            objectMapper,
            consumer::consume,
            MyPojo.class
        );
    }

    public static void entryPoint(SQSEvent event)
    {
        MyLambda.handler.process(event);
    }
}
```
