<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Service introspector

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

        Introspector.DEFAULT_INTROSPECTOR.register(ApiFacade.class, dispatcher);

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

# Client introspector

Similar mechanism exists for the client-side classes. All you have to do is to define an interface (can be also an abstract class) the maps to service methods (you even use same annotations):

```java
import java.util.List;
import java.util.UUID;

import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

public interface MyClient
{
    @JsonRpcCall
    String getName(UUID id);

    @JsonRpcCall(name = "getNameByEmail");
    String getName(String email);

    @JsonRpcCall
    List<String> getNames(@JsonRpcParam(name = "ids") List<UUID> list);
}
```

`@JsonRpcCall` annotation plays similar role that in the service introspector - it marks the method to be handled by the proxy interceptor and invoke the service method. It's additional `name` attribute allows you to change the actual invoked RPC method (by default it's same as method name).

Arguments are mapped by their names and it can also be changed by using `name` attribute of `@JsonRpcParam` annotation.

**Note:** As all **JSON-RPC** mechanisms are handled internally, you don't need to worry about connector anymore - you simply don't need it.

**Note:** Currently only public methods are handled, however it doesn't matter if they are defined directly in a class or inherited.

Not only interfaces are allowed, but also classes, so you can define additional methods that are not mapped to RPC calls. It's especially useful for handling custom, more complicated data types. For instance paged response results need requested page, so this is how we commonly solve the issue:

```java
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.json.ConvertUtils;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;

public abstract class MyClient
{
    @JsonRpcCall(name = "getPage")
    public abstract List<Object> doGetPage(Pageable request);

    public Page<MyEntity> getPage(Pageable request)
    {
        return ConvertUtils.buildPage(
            this.doGetPage(request),
            request,
            MyEntity.class
        );
    }
}
```

## Instantiating the client

Ok, so we don't need to define the logic, all the errorneus and tedious work is done for us. But how to get the actual client instance? That's what you need `pl.chilldev.commons.jsonrpc.client.introspector.Introspector` for (note the different package name than for the service introspector).

```java
import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;

// connector is an instance of pl.chilldev.commons.jsonrpc.client.Connector
MyClient client = Introspector.DEFAULT_INTROSPECTOR.createClient(MyClient.class, connector);

System.out.println(client.getName("test@localhost"));
```

## Parameters mappers

By default all parameters are passed as-they-are to the JSON dumper and they dumped into JSON structure (all non-standard JSON types are dumped by using their `.toString()` method). `Introspector.DEFAULT_INTROSPECTOR` is also capable of handling `org.springframework.data.domain.Pageable` objects (they are dumped as three parameters for page number, page size and sorting criteria, the standard format supported by `commons-jsonrpc` stack. If you want to handle custom parameters in a different way, you can register own parameter mappers. A parameter mapper takes method parameter name, parameter value and current state of request params and is responsible for putting new parameter into the parameter map.

```java
introspector.registerParameterMapper(
    UUID.class,
    (String name, UUID value, Map<String, Object> params) -> {
        params.put(name, value.toString());
    }
);
```

**Note:** Such mapper is not needed as all objects are dumped with `.toString()` by default.

## Response type handlers

It's also possible to handle custom response types. For example `Introspector.DEFAULT_INTROSPECTOR` is capable of returning (apart of standard JSON types) also instances of `java.util.UUID` parsed with `UUID.fromString()` method. You can register your own function for handling custom types:

```java
introspector.registerResultHandler(
    MyEntity.class,
    (Object response) -> ParamsRetriever.OBJECT_MAPPER.convertValue(response, MyEntity.class)
);
```
