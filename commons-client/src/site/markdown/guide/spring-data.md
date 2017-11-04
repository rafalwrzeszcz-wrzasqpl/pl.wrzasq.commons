<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

If you build clients for **Spring Data**-backed services, you very often may need (and want) to paginate the data. [**Spring Data** web support](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.web.basic.paging-and-sorting) handles URL-based paging and sorting based on the parameters. You can of course build URLs with all of these parameters:

```java
interface YourClient
{
    // `PagedResources` is a Spring HATEOAS class
    @RequestLine("GET /users?page={page}&size={size}")
    PagedResources<User> getUsers(@Param("page") long page, @Param("size") long size);
}
```

But it will require from you to put that into every pageable URL. With `pl.chilldev.commons.client.codec.SpringDataEncoder`, you can ommit that and rely on argument of type `Pageable`:

```java
// put your current encoder as fallback to handle all other types
Encoder fallback = new JacksonEncoder(objectMapper);

Encoder encoder = SpringDataEncoder(fallback);
// you can override default parameter names
//Encoder encoder = SpringDataEncoder(fallback, "pageNumber", "limit", "orderBy");

// let's use our factory
FeignClientFactory factory = new BaseFeignClientFactory(
    Collections.singleton(
        builder -> builder.encoder(encoder)
    )
);

// hint: when working with Spring Data Rest you may like to add HAL module to Jackson
objectMapper.registerModule(new Jackson2HalModule());
```

With `SpringDataEncoder` in place you can ommit standard URL parameters:

```java
interface YourClient
{
    @RequestLine("GET /users")
    PagedResources<User> getUsers(Pageable pageRequest);
}

YourClient client = factory.createClient(YourClient.class, "http://your.service.internal");
```

## `commons-data`

Together with `pl.chilldev.commons:commons-data` package you can make it even more **Spring Data**-friendly by utilizing **Java 8** default methods:

```java
interface YourClient
{
    @RequestLine("GET /users")
    PagedResources<User> getUsers(Pageable pageRequest);

    // note that since arguments list is the same we need to pick a new name
    default Page<User> getUsersPage(Pageable pageRequest)
    {
        return ConvertUtils.buildPageFromResources(this.getUser(request), request);
    }
}
```
