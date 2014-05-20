<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Message bag

Although there is nothing strictly web-related in `pl.chilldev.commons.concurrent.collections.MessageBag` class, it's direct purpose is to serve as a flash messages container for web applications.

`MessageBag` is a synchronized wrapper for messages container which stores messages for one-time displaying. Messages are stored by type (currently just a string identifier) - every time you add a message it's stored in the container until you fetch list of messages - then all of them are dropped from the container.

Here is an example of how to add messages to the container:

```java
import pl.chilldev.commons.concurrent.collections.MessageBag;

MessageBag messages = new MessageBag();
messages.addMessage("done", "Connected to server.");
messages.addMessage("done", "Saving list succeeded.");
messages.addMessage("error", "Sending e-mail failed.");
```

To check if container contains any messages to display you may use `hasMessages()` method:

```java
// check if there are any messages
if (messages.hasMessages()) {
    // display messages
}

// check if there are messages of particular type
if (messages.hasMessages("error")) {
    // display error messages
}
```

## MessagesBag.Message

Before we discuss how to display messages there is one additional thing you need to know - although you add just a simple string messages, internall they are stored as a `MessageBag.Message` class instances and this is what you will receive back from the container. This allows us to wrap some additional meta-data to them (for now it's only creation date, but meta info may be extended some day).

Message class contains two methods:

-   `getContent()` which returns string message you added to the container,
-   `getCreatedAt()` which returns time instant at which you added the message.

## Displaying messages

You have two ways for fetching the messages - by type, or all at once. You have to remember that whenever you fetch the messages from ths container, the cotnainer is cleaned afterwards (if you fetch messages by type, only given type is removed). So you should only fetch messages if you are sure you are just about to display them. For checking for messages existance you should use `hasMessages()` method presented above. If, for whatever reason, you already fetched messages and you know your flow will not reach displying the messages (web redirect, exception thrown) you should put messages back to the container.

*Note:* Both `getMessages(type)` and `getAllMessages()` are `NULL`-safe, which means they always return containers (`List` and `Map` respectively), at least empty ones if there are no messages. Effectively, this means that if you are sure you are just about to display messages, you don't need to check if there are any messages in the container (unless you want to produce some additional markup for the container etc.).

Fetching by type:

```java
import java.util.List;

import pl.chilldev.commons.concurrent.collections.MessageBag;

// displaying messages of given type
List<MessageBag.Message> firstList = messages.getMessages("done");
List<MessageBag.Message> secondList = messages.getMessages("done");

for (MessageBag.Message message : firstList) {
    System.out.println("SUCCESS: " + message.getContent());
}

// assuming there is no un-synchronized operation between two fetched above
// secondList will always be empty
for (MessageBag.Message message : secondList) {
    System.out.println("SUCCESS: " + message.getContent());
}
```

Fetching all the messages:

```java
import java.util.List;
import java.util.Map;

import pl.chilldev.commons.concurrent.collections.MessageBag;

// displaying messages of given type
Map<String, List<MessageBag.Message>> firstMap = messages.getAllMessages();
Map<String, List<MessageBag.Message>> secondMap = messages.getAllMessages();

for (Map.Entry<String, MessageBag.Message> entry : firstMap.entrySet()) {
    for (MessageBag.Message message : entry.getValue()) {
        System.out.println("[%s] %s".format(entry.getKey(), message.getContent()));
    }
}

// assuming there is no un-synchronized operation between two fetched above
// secondMap will always be empty
for (Map.Entry<String, MessageBag.Message> entry : secondMap.entrySet()) {
    // nothing to do here
}
```

*Note:* If you fetch all messages (`getAllMessages()` method) don't relay on `Map.isEmpty()` method! Although all message instances are removed from the container after being fetched, to save operations empty lists are not removed - they don't disturb in anything, so they are kept internally as most likely you will use same message types all the time. Because of that, even if there are no messages within the returned map, the map itself may not be empty because of containing empty lists.

## Spring integration

To integrate `MessageBag` into *Spring* you just need to add service definition (you may want to add `id` attribute if you want to refer to it within DI):

```xml
<bean class="pl.chilldev.commons.concurrent.collections.MessageBag" scope="session"/>
```

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
