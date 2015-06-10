<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Apache MINA I/O

[**Apache MINA**](https://mina.apache.org) is perfect for building **JSON-RPC** services as it brings asynchronous network I/O stack without footprint of enclosing protocol (like **HTTP**) - you can use it to build plain **TCP** service. There are two classes that will help you bind your RPC `Dispatcher` with MINA socket. First is `pl.chilldev.commons.jsonrpc.mina.DispatcherIoHandler` which binds to MINA socket and handles requests using JSON-RPC dispatcher provided by you; second is `pl.chilldev.commons.jsonrpc.mina.IoServiceUtils`, which is a simple utility class that configures the socket parameters.

```java
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import pl.chilldev.commons.jsonrpc.mina.DispatcherIoHandler;
import pl.chilldev.commons.jsonrpc.mina.IoServiceUtils;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class NetworkService
    implements
        IoServiceUtils.Configuration
{
    @Override
    public int getMaxPacketSize()
    {
        // 1MiB
        return 1048576;
    }

    public void start(Dispatcher<YourContextType> dispatcher, YourContextType context)
        throws
            Exception
    {
        // this is pure MINA socket handler
        NioSocketAcceptor acceptor = new NioSocketAcceptor();

        try {
            // network service configuration
            IoServiceUtils.initialize(
                acceptor,
                // this is the most important line
                // it registers our IoHandler that will dispatch requests from MINA socket using given dispatcher
                new DispatcherIoHandler<YourContextType>(context, dispatcher),
                // about this argument read the next paragraph
                this
            );
            acceptor.setReuseAddress(true);
            // run the server
            acceptor.bind(this.address);

            // it should be run as a thread, the example is simplified
            while (!this.interrupted()) {
                this.sleep(this.sleepTick);
            }
        } finally {
            // close socket
            acceptor.unbind();
            acceptor.dispose();
        }
    }
}
```

**Note:** The snippet above is the standard implementation available out of the box with <a href="./listener.html">listener</a>.

**Note:** `DispatcherIoHandler` handles all exceptions thrown during request processing (if not reported properly as error responses) and reports them as internal error to the client to avoid listener thread to die. Returned error has error code `-1` (see <a href="./rpc.html">error codes</a>).

## `IoServiceUtils.Configuration`

The example is rather straightforward, despite some boilerplate code. But one thing may not be clear - the third parameter of `IoServiceUtils.initialize()`, the `IoServiceUtils.Configuration` interface implementation and `getMaxPacketSize()` method (yes, in fact it's just one thing!). One of the parameters `IoServiceUtils.initialize()` method set is maximum packet size. It does that based on configuration object passed as a thord argument, which has to implement `IoServiceUtils.Configuration` interface. All you need to do is simply implement the `getMaxPacketSize()` that will return your desired packet size limit.
