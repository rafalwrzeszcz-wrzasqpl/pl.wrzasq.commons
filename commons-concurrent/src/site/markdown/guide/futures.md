<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @author Rafał Wrzeszcz <rafal.wrzeszcz@wrzasq.pl>
# @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
# @version 0.0.1
# @since 0.0.1
# @category ChillDev-Commons
# @subcategory Concurrent
-->

# Futures

Futures are base concept allowing asynchronous tasks processing to *Java*. *ChillDev-Commons-Concurrent* provides classes that wrap them for some commonly faced tasks.

## FutureResponder

One particular schema that is covered by this package is mixing futures with synchronous code when your future processing is being done synchronously.

Particular example of such case can be an asynchronous *RPC* service that is being build on top of synchronous core - your listener needs to react asynchronously for the I/O, but underneath you have a procedural imperative processing engine. Your listener creates future and postpones rendering the response, while moving for handling next request and your core starts to process new future request. Of course you could implement your processing logic as a sub-class of `java.util.concurrent.FutureTask`, but that leads to two major problems:

1.  first of all your listener needs to be aware of what type of futures to create - usually this is not the case, because futures are created by I/O handlers, not the application protocol wrappers and they shouldn't be involved in application logic/models creation;
1.  when you use 3rd-party libraries to handle protocols they are also unaware of your custom implementations and create always generic (not necessarily in terms of Java generics) types which you need to process.

Solution for both of these prolems is `pl.chilldev.commons.concurrent.FutureResponder`. It is a thin, generic wrapper that allows you to create broker object and make both sides of evaluation (both listener and worker) process to act as an asynchronous tasks from two sides of the future object. It keeps future object marked unready as long as (not-so-)asynchronous processing logic will set result of a computation.

Here is example asynchronous client implementation using *Apache Mina*:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import org.apache.mina.core.session.IoSession;

import pl.chilldev.commons.concurrent.FutureResponder;

import my.package.RequestType;
import my.package.ResponseType;

public class ServiceClient
{
    protected static long id;
    protected Queue<RequestType> requests = new ConcurrentLinkedQueue<>();
    protected Map<Object, FutureResponder<ResponseType>> responses = new HashMap<>();

    public FutureTask<ResponseType> execute(RequestType request)
    {
        long id = ServiceClient.id++;
        FutureResponder<ResponseType> responder = new FutureResponder<>();
        FutureTask<ResponseType> future = new FutureTask<>(responder);
        responder.setFuture(future);
        this.requests.add(request);
        this.responses.put(id, responder);
        return future;
    }

    public void sessionOpened(IoSession session)
    {
        RequestType request;
        while ((request = this.requests.poll()) != null) {
            session.write(request);
        }
    }

    public void messageReceived(IoSession session, Object message)
    {
        // parse the response
        ResponseType response = ResponseType.parse(message.toString());

        // this is our magic line!
        // dispatch it -- setting the response result fires future
        this.responses.get(response.getID()).setResponse(response);
        this.responses.remove(response.getID());
    }
}

/*
sample client usage - it's not really the full example as we lack Apache Mina wrapper,
code looks completely synchronous-way:

ServiceClient client = new ServiceClient();
FutureTask<ResponseType> future = client.execute(request);
ResponseType response = future.get();
*/
```
