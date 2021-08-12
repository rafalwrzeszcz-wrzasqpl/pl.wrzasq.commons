<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Feign factory

Entire `commons-client` package is oriented around [**Feign**](https://github.com/OpenFeign/feign). The main class to
work with it is `pl.wrzasq.commons.client.FeignClientFactory`. It provides a way to automate your `Feign.builder()`
customization. It's mainly useful when you develop multi micro-service system with a lot of clients sharing same
serialization handling, authorization flow etc.

## `BaseFeignClientFactory`

In most cases you can use `pl.wrzasq.commons.client.FeignClientFactory`, which creates a client using `Feign.Builder`.
Thanks to that all you need to do is to specify your configurators. Configurators are callbacks that operate on *Feign*
client builder to set it up. By creating a factory with given set of configurators you are sure that all clients created
by the factory will be configured the same way. For example if you always need to set your **HTTP** client as
`ApacheHttpClient`, if you want to apply your custom error decoder:

```kotlin
val yourConfigurators = listOf
    // note that you can write plain lambda expressions, as we use plain `Consumer<>` interface
    {
        it.client(
            ApacheHttpClient(
                // this is AWS X-Ray enabled client builder
                HttpClientBuilder.create().build()
            )
        )
    }

val factory = FeignClientFactory(yourConfigurators)

val client1 = factory.createClient(YourClient1::class.java, "http://service1.internal")
val client2 = factory.createClient(YourClient2::class.java, "http://service2.internal")
```

In the example above all clients created with the factory will use
[**AWS X-Ray**-instrumented Apache HTTP client](http://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-httpclients.html).

## Client-specific setup

Sometimes there is still need for some client-specific customization that won't be applied to other instances (date
formatting? null-handling? case convention?). You can pass custom configurators (or just build an inline lambda
expression) to customize the builder before creating the client you want:

```kotlin
val factory = BaseFeignClientFactory(yourConfigurators)

// this will apply only default configurators (`yourConfigurators`)
val client1 = factory.createClient(YourClient1::class.java, "http://service1.internal")
// this will apply both `yourConfigurators` and `customConfigurators`
val client2 = factory.createClient(YourClient2::class.java, "http://service2.internal", customConfigurators)
// this will apply `yourConfigurators and apply lambda
val client3 = factory.createClient(
    YourClient3::class.java,
    "http://service3.internal") {
    it
        .logger(Slf4jLogger(YourClient3::class.java))
        .decoder(JacksonDecoder(objectMapper))
}
```

Client-specific configurators are applied after default ones, so you can use them to override your generic setup.

## Custom `Feign.Builder`

What if you want to customize `Feign.Builder` itself? You specify builder supplier, when you create a factory:

```kotlin
val factory = FeignClientFactory(
    yourConfigurators,
    HystrixFeign::builder // this is a regular Supplier<>, you can write own lambda
)
```
