<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Publishing

When you deal with [**SNS**](https://aws.amazon.com/sns/) you most likely transfer messages with the form of **JSON**. Also you usually work against specified SNS topic (which means it's ARN won't change). `pl.wrzasq.commons.aws.sns.TopicClient` is a simple wrapper that wraps single topic communication and handles **JSON** conversion:

```java
class MyPing
{
    public String message;
    public String callback;

    public MyPing(String message, String callback)
    {
        this.message = message;
        this.callback = callback;
    }
}

class MyProducer
{
    private TopicClient client;

    public MyProducer(TopicClient client)
    {
        this.client = client;
    }

    public void produce()
    {
        // (2)
        this.client.send(new MyPing("hello", "http://localhost/ping"));
    }
}

public class MyLambda
{
    private static MyProducer producer = new MyProducer(
        // (1)
        new TopicClient("arn:test")
    );

    public static void entryPoint()
    {
        // (3)
        MyLambda.producer.produce();
    }
}
```

1.  You can initialize `TopicClient` with custom **Jackson** `ObjectMapper` to handle your custom types/strategies.
1.  Note that you don't need to pass topic ARN around - it's wrapped in the client.
1.  See that your logic has now no call-time parameters, it's all covered by underlying client.

# Handling events

To process incoming events you can use `pl.wrzasq.commons.aws.sns.NotificationHandler` class - it extracts **SNS** records from the event and invokes message handler for each of them:

```java
class MyConsumer
{
    public void consume(SNSEvent.SNS message)
    {
        // handle single message
    }
}

public class MyLambda
{
    private static NotificationHandler handler;

    static {
        MyConsumer consumer = new MyConsumer();

        // (1)
        MyLambda.handler = new NotificationHandler(consumer::consume);
    }

    public static void entryPoint(SNSEvent event)
    {
        // (2)
        MyLambda.handler.process(event);
    }
}
```

1.  All you expose to the handler is single message consumer method, you keep your components separated.
1.  Your **Lambda** entry point gets lean.

## Simple notifications handling

If you are only interested in notification body handling, you can use even simpler wrapper `SimpleNotificationHandler`, which also extracts message body, giving you just it's content:

```java
public class MyLambda
{
    private static NotificationHandler handler;

    static {
        // all you have is plain string
        MyLambda.handler = new SimpleNotificationHandler(
            (String content) -> System.out.println(content)
        );
    }

    public static void entryPoint(SNSEvent event)
    {
        MyLambda.handler.process(event);
    }
}
```

## Typed messages

This is still not all - you can also define typed message consumer that will automatically handle your **JSON** handling:

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
    private static NotificationHandler handler;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        // configure your object mapper

        MyConsumer consumer = new MyConsumer();
        // configure your consumer

        // (2)
        MyLambda.handler = new TypedNotificationHandler(
            objectMapper,
            consumer::consume,
            MyPojo.class
        );
    }

    public static void entryPoint(SNSEvent event)
    {
        MyLambda.handler.process(event);
    }
}
```

1.  In your logic you get your unserialized data model.
1.  All you need to do is to plug your entry point.
