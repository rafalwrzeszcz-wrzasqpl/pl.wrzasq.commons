<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Client handler

Although `commons-jsonrpc` package mainly focuses on building services, it provides also base classes for building clients for those services. It's especially helpful because uses the same networking library - [**Netty**](http://netty.io). First of all it allows you to minimize dependencies and secondly - it brings asynchronicity to your client out of the box.

Base class that encodes all calls as JSON-RPC requests is `pl.chilldev.commons.jsonrpc.netty.RequestHandler`. It returns future objects that are fullfilled once the response for given request is received.

```java
RequestIoHandler handler = new RequestIoHandler();

// establish Netty connection
// set handler as your connection handler

// sends JSON-RPC request for `getVersion()` method without params
FutureTask<JSONRPC2Response> future = handler.execute("getVersion");

// proceed with your stuff, until you really need the respnse

// get the response
JSONRPC2Response response = future.get();
```

## Connector

`RequestIoHandler` class is responsible for JSON-RPC requests handling. But if your case is not a specific there is also additional wrapper `pl.chilldev.commons.jsonrpc.client.Connector` class that provides a convenient API for building full TCP JSON-RPC clients out of the box - all you need to do is to specify service address (host and port).

**Note:** Right now this class has a major disadvantage, as it synchronizes response flow (which means you loose JSON-RPC big advantage which is asynchronicity).

**Note:** Client will connect automatically on first request and reconnect everytime it's needed (like after connection is lost).

It provides `execute(String method)` method which executes method on RPC service and returns response results (there is also overloaded signature with method parameters map).

```java
// constructor is quite verbose
Connector connector = new Connector(
    yourNettyNioSocketConnectorBootstrap,
    yourRequestIoHandlerInstance,
    new InetSocketAddress("api.domain.com", 1234)
);

// but there is factory method
// as in most cases you don't need to interact with Netty socket and request handler directly
// factory method creates default instances for you
Connector connector = Connector.create(new InetSocketAddress("api.domain.com", 1234));

// if you want to connect manually you need to combine two methods:
// - connector.connect() returns connection future
//      it will be fulfilled once connection is established (or fails)
// - connector.reconnect(future) waits for session from connection future
//      it will wait until connection future is fulfilled and grab session from it
connector.reconnect(connector.connect());

Object result = connector.execute("methodName");
```

**Note:** Client connector is un-aware of any response type, it always return instance of `java.lang.Object`. You need to cast this object afterwards on your own.
