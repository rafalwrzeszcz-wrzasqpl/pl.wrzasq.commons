<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Feign factory

Entire `commons-client` package is oriented around [**Feign**](https://github.com/OpenFeign/feign). The main class to work with it is `pl.wrzasq.commons.client.FeignClientFactory`. It provides a way to automate your `Feign.builder()` customization. It's mainly useful when you developer multi micro-service system with a lot of clients sharing same serialization handling, authorization flow etc.

## `BaseFeignClientFactory`

In most cases you can use `pl.wrzasq.commons.client.BaseFeignClientFactory`, which creates uses `Feign.Builder`. Thanks to that all you need to do is to specify your configurators. Configurators are callbacks that operate on *Feign* client builder to set it up. By creating a factory with given set of configurators you are sure that all clients created by the factory will be configured the same way. For example if you always need to set your **HTTP** client as `ApacheHttpClient`, if you want to apply your custom error decoder:

```java
Collection<Consumer<Feign.Builder>> yourConfigurators = new ArrayList<>();
// note that you can write plain lambda expressions, as we use plain `Consumer<>` interface
yourConfigurators.add(
    builder -> builder
        .client(
            new ApacheHttpClient(
                // this is AWS X-Ray enabled client builder
                HttpClientBuilder.create().build()
            )
        )
);

FeignClientFactory factory = new BaseFeignClientFactory(yourConfigurators);

YourClient1 client1 = factory.createClient(YourClient1.class, "http://service1.internal");
YourClient2 client2 = factory.createClient(YourClient2.class, "http://service2.internal");
```

In the example above all clients created with the factory will use [**AWS X-Ray**-instrumented Apache HTTP client](http://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-httpclients.html).

## Client-specific setup

Sometimes there is still need for some client-specific customization that won't be applied to other instances (date formatting? null-handling? case convention?). You can pass custom configurators (or just build an inline lambda expression) to customize builder before creating the client you want:

```java
FeignClientFactory factory = new BaseFeignClientFactory(yourConfigurators);

// this will apply only default configurators (`yourConfigurators`)
YourClient1 client1 = factory.createClient(YourClient1.class, "http://service1.internal");
// this will apply both `yourConfigurators` and `customConfigurators`
YourClient2 client2 = factory.createClient(YourClient2.class, "http://service2.internal", customConfigurators);
// this will apply `yourConfigurators and apply lambda
YourClient3 client3 = factory.createClient(
    YourClient3.class,
    "http://service3.internal",
    builder -> builder
        .logger(new Slf4jLogger(YourClient3.class))
        .decoder(new JacksonDecoder(objectMapper))
);
```

Client-specific configurators are applied after default ones, so you can use them to override your generic setup.

## Custom `Feign.Builder`

What if you want to customize `Feign.Builder` itself? Then you can use `FeignClientFactory` directly. You specify builder supplier, when you create a factory:

```java
FeignClientFactory factory = new FeignClientFactory(
    yourConfigurators,
    HystrixFeign::builder // this is a regular Supplier<>, you can write own lambda
);
```
