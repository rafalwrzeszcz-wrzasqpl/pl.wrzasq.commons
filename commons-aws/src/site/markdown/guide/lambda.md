<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Handler delegation

**Note:** Your **Lambda** function should server single purpose and should handle single type of data payload. The
reason this delegation exists is only to allow non-business related requests, mostly related to infrastructure layer
(like pre-warming).

If your **Lambda** handler is about to accept multiple types of payload it may be difficult to write a combined handler
that detects payload type and switches handling logic. This is what `pl.wrzasq.commons.aws.lambda.MultiHandler` does.
Your sub-handlers receive parsed **JSON** tree as an argument and decide if they handle given payload or not.

Each sub-handler responds with boolean value. When the request is handled completely by given sub-handler it should
return true to stop further propagation to next handlers.

```java
class MyHandler extends MultiHandler {
    public MyHandler() {
        super(new ObjectMapper());
        
        this.registerHandler(this.handlePut);
        this.registerHandler(this.handleSns);
    }
    
    public boolean handlePut(JsonNode root, OutputStream output) {
        // handle PUT request
    }
    
    public boolean handleSns(JsonNode root, OutputStream output) {
        // extract payload (eg. with pl.wrzasq.commons.aws.sns.NotificationHandler)
        
        this.handlePut(extractedPayload, output);
    }
}
```

The entry point of `MultiHandler` (to be used as **Lambda** handler) is
`pl.wrzasq.commons.aws.lambda.MultiHandler::handle`.

# Hearbeat (aka pre-warming) handler

Specific case, for which in particular the situation of handling different types of payload was introduced to our stack
is a [per-warming event for **Lambda**](https://www.jeremydaly.com/lambda-warmer-optimize-aws-lambda-function-cold-starts/).
It needs to be handled independently from any other payload type.

```java
class MyHandler extends MultiHandler {
    public MyHandler() {
        super(new ObjectMapper());
        
        this.registerHandler(new HeartbeatHandler("eventType", "heartbeat"));
        this.registerHandler(this.handleLogic);
    }
    
    public boolean handleLogic(JsonNode root, OutputStream output) {
        // your business logic
    }
}
```

In such case, for event payload:

```json
{
    "eventType": "heartbeat"
}
```

Your business logic will not even be triggered.

By default (if you construct `HeartbeatHandler` without arguments) it looks for field name `wrzasqpl:event:type` and
value `wrzasqpl:heartbeat`.
