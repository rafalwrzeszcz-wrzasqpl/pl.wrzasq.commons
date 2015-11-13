<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# JSON-RPC introspector

With all the knowledge about how the dispatcher work, we can go step further and cover all of that with the introspection mechanism that will compose the dispatcher for us. For that we will have to use `pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector` class. It takes the class, looks for all it's methods that are annotated with `pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall` annotation and registers request handler for every such method. The logic here is as follows:

-   method handler is registered in the dispatcher with the same name (possible to change via annotation);
-   for each method argument an RPC argument resolver is registered matching it's type;
-   arguments resolving is done based on method arguments names (possible to change via annotation);
-   method result is returned as a response (possible to be post-processed, unless `void`);
-   `JSONRPC2Error` exceptions are exposed directly through respose, others are wrapped with `JSONRPC2Error.INTERNAL_ERROR`.

**Note:** As arguments resolving is by default done based on their names, your code needs to be compiled with `-parameters` argument to expose parameters names at runtime via reflection API. Otherwise you will have to explicitly define names for all arguments through annotations.

In order to use introspector, you first need to create a facade class (or interface) that will describe the API you want to expose in your dispatcher:

```java
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;

public interface ApiFacade
{
    @JsonRpcCall
    public String getPath(UUID id);

    // name property of the annotation allows you to override the JSON-RPC method name
    @JsonRpcCall(name = "getPathByName")
    public String getPath(String name);

    // note lack of @JsonRpcCall annotation - this method won't be exposed
    public String getPath();
}
```

Once you have the class/interface, you may use introspector to populate the disptcher:

```java
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;

public class DispatcherFactory
{
    public Dispatcher<ApiFacade> createDispatcher()
    {
        Dispatcher<ApiFacade> dispatcher = new Dispatcher<>();
        Introspector introspector = Introspector.createDefault();

        introspector.register(ApiFacade.class, dispatcher);

        // you can register owne method handlers

        return dispatcher;
    }
}
```

**Note:** Introspector just populates the dispatcher, you can register own request handlers in the same dispatcher as well.

## Fine-tuning parameters

Apart from `@JsonRpcCall` annotation there is a second one, `@JsonRpcParam`, which allows for defining detailed metadata for each of the method parameters.

**Note:** By default each method parameter is mendatory, however default status of `optional` flag of `@JsonRpcParam` annotation is `true`, so using this annotation on it's own automatically makes parameter optional.

The annotation has four attributes:

-   `String name` - can be used to override parameter name in JSON-RPC signature;
-   `boolean optional` - optionality flag;
-   `String defaultValue` - default value of the parameter (if optional) - due to **Java** limitations it must always be a `String`;
-   `boolean defaultNull` - use this flag to set default parameter value to `null`, it has a precedence over `defaultValue` (it's introduced because of another **Java** stupid limitation, that annotation properties may not have `null` values).

```java
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

public interface ApiFacade
{
    @JsonRpcCall
    public String calculateHash(
        // this one is mendatory, this is the default behavior
        String base,
        // this one is optional, default value is true
        @JsonRpcParam(defaultValue = "true") boolean optionalBool,
        // this one is explicitely mendatory and fetched by different name
        @JsonRpcParam(name = "name", optional = false) String id,
        // this one is optional and by default it's null
        @JsonRpcParam(defaultNull = true) String optionalString
    );
}
```

## Parameters providers

To handle method argument, a parameter provider needs to be registered for it's type. Parameter provider is responsible for retrieving parameter value from request based on it's name (and additional information, like default value).

Parameter provider takes four arguments:

1.  request parameter name;
1.  request parameters retriever;
1.  optionality flag;
1.  default value as a `String`.

```java
introspector.registerParameterProvider(
    YourType.class
    (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
        String value = optional ? params.getOptString(name, defaultValue) : params.getString(name);
        return YourType::fromString(value);
    }
);
```

Because of **Java** limitations regarding annotations, `defaultValue` will be a `java.lang.String` (may be `null`), you must decide on your own how to represent it in your type.

**Note:** You have to register parameter provider before using introspector on your facade.

**Note:** If any of arguments of your facade won't have registered provider, introspector will throw exception on attempt to register that facade.

## Response mappers

It is also possible to register mappers for post-processing your response. It's mainly usable if you don't want to design your facades especially for **JSON-RPC** protocol and want to expose your standard methods that may return complex types. For instance standard introspector is capable of dumping `org.springframework.data.domain.Page` objects as a JSON-able structure.

Response type mapper is a simple mapping function that maps single argument (which is the invoked method result) into another value (that will be assigned as JSON-RPC response). It is also registered based on it's type:

```java
introspector.registerResultMapper(
    YourEntity.class,
    (YourEntity entity) -> new YourTransferPojo(entity)
);
```

**Note:** You have to register results mapper before using introspector on your facade.