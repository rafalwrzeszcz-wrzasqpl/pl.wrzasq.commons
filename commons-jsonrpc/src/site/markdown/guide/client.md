<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Client connector

Although `commons-jsonrpc` package mainly focuses on building services, it provides also base classes for building clients for those services. It's especially helpful because uses the same networking library - [**Apache MINA**](https://mina.apache.org). First of all it allows you to minimize dependencies and secondly - it brings asynchronicity to your client out of the box.

Base class that encodes all calls as JSON-RPC requests is `pl.chilldev.commons.jsonrpc.mina.RequestIoHandler`. It returns future objects that are fullfilled once the response for given request is received.

```java
RequestIoHandler handler = new RequestIoHandler();

// establish MINA connection
// set handler as your connection handler

// sends JSON-RPC request for `getVersion()` method without params
FutureTask<JSONRPC2Response> future = handler.execute("getVersion");

// proceed with your stuff, until you really need the respnse

// get the response
JSONRPC2Response response = future.get();
```
