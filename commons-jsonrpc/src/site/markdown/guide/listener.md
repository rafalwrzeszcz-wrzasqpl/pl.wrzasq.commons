<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Listener

If you just want to build own, stand-alone **JSON-RPC** daemon, `pl.chilldev.commons.jsonrpc.daemon.Listener` is all you need to bind your dispatcher to listen on the network address. Listener is specified to listen for ongoing connection on given port and react on them. All you need to do is to start it with specified address to listen on:

```java
import java.net.InetSocketAddress;

import pl.chilldev.commons.jsonrpc.daemon.Listener;

public class App
{
    public static void main(String[] args)
    {
        Listener<YourContextType> listener = new Listener<>("listenerName", yourContext, yourDispatcher);
        listener.setAddress(new InetSocketAddress("localhost", 1234));
        listener.start();

        // do your stuff

        // this tells listener to stop the server
        listener.stop();
    }
}
```

You can tune server settings with:

-   `Listener.setMaxPacketSize(int maxPacketSize)` - to change maximum JSON-RPC packet size (defaults to **32MiB**).

## Execution context

Ok, what's with this `YourContextType` class, finally? This is the execution context that will be passed to all RPC request handlers. This allows you to build service-agnostic request handlers and dispatchers - you can build universal service with multiple listeners running with same **JSON-RPC** dispatcher (and thus same request handlers) on different sockets. For example run same JSON-RPC service on shared server with different execution context for each project.

Context is bound to `DispatcherHandler` instance (`Listener` builds own `DispatcherHandler` internally), not to the `Dispatcher` one, so the dispatcher (together with request handlers) can simply operate on resources of each of contexts separately.

Your context need to implement interface `pl.chilldev.commons.jsonrpc.daemon.ContextInterface` (for now it's empty interface, just used as a marker):

```java
import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;

public class YourContextType implements ContextInterface
{
    // provide any resources you need to expose in method handlers
}
```

You can then create separate contexts for each listener that use the dispatcher.

```java
Dispatcher<YourContextType> dispatcher = new Dispatcher<>();
dispatcher.register("createUser", new Dispatcher.RequestHandler<YourContextType>() {
    public JSONRPC2Response process(JSONRPC2Request request, YourContextType context)
        throws
            JSONRPC2Error
    {
        // context depends on the fact from which Listener this method was called
    }
});

YourContextType context1 = new YourContextType();
// set context1 for projectFoo

YourContextType context2 = new YourContextType();
// set context2 for projectBar

YourContextType context3 = new YourContextType();
// set context3 for projectBaz

// note that we use same dispatcher object everywhere, you may be sure that all services will expose same API
Listener<YourContextType> listener1 = new Listener<>("rpc-wrzasq.pl", context1, dispatcher);
Listener<YourContextType> listener2 = new Listener<>("rpc-chilldev.pl", context2, dispatcher);
Listener<YourContextType> listener3 = new Listener<>("rpc-ikancelaria.com", context3, dispatcher);
```
