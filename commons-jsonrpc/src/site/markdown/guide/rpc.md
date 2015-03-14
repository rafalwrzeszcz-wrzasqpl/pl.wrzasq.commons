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
    public

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
