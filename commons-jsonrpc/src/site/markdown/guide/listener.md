<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Execution context

Ok, what's with this `YourContextType` class, finally? This is the execution context that will be passed to all RPC request handlers. This allows you to build service-agnostic request handlers and dispatchers - you can build universal service with multiple listeners running with same **JSON-RPC** dispatcher (and thus same request handlers) on different sockets. For example run same JSON-RPC service on shared server with different execution context for each project.

Context is passed bound to `IoHandler` instance, not to the `Dispatcher` one, so the dispatcher (together with request handlers) can simply operate on resources of each contexts separately.

Your context need to implement interface `pl.chilldev.commons.jsonrpc.daemon.ContextInterface` (for now it's empty interface, just used as a marker):

```java
pl.chilldev.commons.jsonrpc.daemon.ContextInterface;

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
        // context depends on the fact from which IoHandler this method was called
    }
});

YourContextType context1 = new YourContextType();
// set context1 for projectFoo

YourContextType context2 = new YourContextType();
// set context2 for projectBar

YourContextType context3 = new YourContextType();
// set context3 for projectBaz

// note that we use same dispatcher object everywhere, you may be sure that all services will expose same API
IoHandler<YourContextType> handler1 = new IoHandler<>(context1, dispatcher);
IoHandler<YourContextType> handler2 = new IoHandler<>(context2, dispatcher);
IoHandler<YourContextType> handler3 = new IoHandler<>(context3, dispatcher);
```
