<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Publishing

You can use `pl.wrzasq.commons.aws.sqs.QueueClient` to bind target queue URL with a client and avoid passing
excessive information around. `QueueClient` also handles **JSON** serialization of the payload:

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

    public MyProducer(QueueClient client)
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
        new QueueClient("https://target/")
    );

    public static void entryPoint()
    {
        // (3)
        MyLambda.producer.produce();
    }
}
```

1.  You can initialize `QueueClient` with custom **Jackson** `ObjectMapper` to handle your custom types/strategies.
1.  Note that you don't need to pass queue URL around - it's wrapped in the client.
1.  See that your logic has now no call-time parameters, it's all covered by underlying client.

# Handling messages

If you want your **Lambda** to process [**SQS**](https://aws.amazon.com/sqs/) messages you can use
`pl.wrzasq.commons.aws.sqs.EventHandler` class to implement your function as an event source. Flow here is similar to
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
