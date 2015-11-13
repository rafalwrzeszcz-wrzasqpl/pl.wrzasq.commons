<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# JSON-RPC dispatcher

[**JSON-RPC**](http://www.jsonrpc.org/specification) dispatcher is an object that handles JSON requests and dispatches them to associated method handlers - it's like the router for you JSON-RPC server. First thing you need to do is to implement method handlers (don't worry about `YourContextType` class, we will discuss that further):

```java
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class CreateUserHandler
    implements
        Dispatcher.RequestHandler<YourContextType>
{
    @Override
    public JSONRPC2Response process(JSONRPC2Request request, YourContextType context)
        throws
            JSONRPC2Error
    {
        try {
            // handle request

            // return valid response
            // true is your response data, can be anything
            return new JSONRPC2Response(true, request.getId());
        } catch (SomeException error) {
            return new JSONRPC2Error(errorCode, error.getMessage());
        }
    }
}
```

All you need to do is to register request handlers for particular method names:

```java
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class DispatcherFactory
{
    public Dispatcher<YourContextType> createDispatcher()
    {
        Dispatcher<YourContextType> dispatcher = new Dispatcher<>();
        dispatcher.register("createUser", new CreateUserHandler());
        // you can register more method handlers
        return dispatcher;
    }
}
```

That's all you need to do with the dispatcher object. It's usage is quite simple - it just handles the request object by mapping it's request method to registred handlers but it's done internally.

## Method wrappers

But yet, ususally you don't need such low-level response control - response ID must always match current request ID and returned value is usually simply your method call result. So most likely generating response based on your method call is a straight-forward task and there is no reason why you should need to handle it manually every time. For that there are two interfaces with dedicated request handlers - one for methods that generate response result (`pl.chilldev.commons.jsonrpc.rpc.ReturningMethod`) and one for void methods (`pl.chilldev.commons.jsonrpc.rpc.VoidMethod`).

**Note:** If you want to return `NULL` value it's a return value so you should use returning method interface - void-method indicated no response result at all (JSON-RPC response has no `result` property in such case).

These two interfaces are automatically wrapped by dispatcher, so you can still build your dispatcher with just your method objects:

```java
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;
import pl.chilldev.commons.jsonrpc.rpc.ReturningMethod;
import pl.chilldev.commons.jsonrpc.rpc.VoidMethod;

public class DispatcherFactory
{
    public Dispatcher<YourContextType> createDispatcher()
    {
        Dispatcher<YourContextType> dispatcher = new Dispatcher<>();
        dispatcher.register("rawRpcHandler", new PlainCreateUserHandler());
        dispatcher.register("returningMethodName", new MyReturningMethod());
        dispatcher.register("voidMethodName", new MyVoidMethod());
        // you can register more method handlers
        return dispatcher;
    }
}

class MyReturningMethod
    implements
        ReturningMethod<YourContextType>
{
    @Override
    public Object process(JSONRPC2Request request, YourContextType context)
        throws
            JSONRPC2Error
    {
        try {
            // handle request

            // return valid response
            // this is your response data
            // can be anything, returning type enforced by interface is just `java.lang.Object`
            // it will be used directly as response result
            "Hell world";
        } catch (SomeException error) {
            return new JSONRPC2Error(errorCode, error.getMessage());
        }
    }
}

class MyVoidMethod
    implements
        VoidMethod<YourContextType>
{
    @Override
    public void process(JSONRPC2Request request, YourContextType context)
        throws
            JSONRPC2Error
    {
        try {
            // handle request
            // just don't return anything
            // response will be generated with no result automatically
        } catch (SomeException error) {
            return new JSONRPC2Error(errorCode, error.getMessage());
        }
    }
}
```

## Error codes

Additionally, to unify some aspects of errors handling `pl.chilldev.commons.jsonrpc.rpc.ErrorCodes` class provides standard error codes:

Error code | Error code | Exception property | Scenario
--- | --- | --- | ---
`-1` | `CODE_CONNECTION` | `ERROR_CONNECTION` | This error is returned in case of connection error while waiting for the response. It's in fact not returned by the server, but rendered internally to unify error handling strategy against network protocol.
