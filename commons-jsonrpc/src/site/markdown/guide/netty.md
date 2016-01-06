<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Netty I/O

[**Netty**](http://netty.io) is perfect for building **JSON-RPC** services as it brings asynchronous network I/O stack without footprint of enclosing protocol (like **HTTP**) - you can use it to build plain **TCP** service. There are two classes that will help you bind your RPC `Dispatcher` with Netty socket. First is `pl.chilldev.commons.jsonrpc.netty.DispatcherHandler` which binds to Netty socket and handles requests using JSON-RPC dispatcher provided by you; second is `pl.chilldev.commons.jsonrpc.netty.StringChannelInitializer`, which is a simple utility class that configures the socket parameters.

```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import pl.chilldev.commons.jsonrpc.netty.DispatcherHandler;
import pl.chilldev.commons.jsonrpc.netty.StringChannelInitializer;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class NetworkService
    implements
        StringChannelInitializer.Configuration
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
        // these are pure Netty thread groups
        EventLoopGroup acceptors = new NioEventLoopGroup();
        EventLoopGroup workers = new NioEventLoopGroup();

        try {
            // network service configuration
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                .group(acceptors, workers)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(
                    new StringChannelInitializer<Channel>(
                        new DispatcherHandler<ContextType>(this.context, this.dispatcher),
                        this
                    )
                );
            bootstrap
                .bind(this.address)
                .sync()
                .channel()
                .closeFuture()
                .sync();
        } finally {
            // close connections
            acceptors.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }
}
```

**Note:** The snippet above is the standard implementation available out of the box with <a href="./listener.html">listener</a>.

**Note:** `DispatcherHandler` handles all exceptions thrown during request processing (if not reported properly as error responses) and reports them as internal error to the client to avoid listener thread to die. Returned error has error code `-1` (see <a href="./rpc.html">error codes</a>).

## `StringChannelInitializer.Configuration`

The example is rather straightforward, despite some boilerplate code. But one thing may not be clear - the second parameter of `StringChannelInitializer()` constructor, the `StringChannelInitializer.Configuration` interface implementation and `getMaxPacketSize()` method (yes, in fact it's just one thing!). One of the parameters `StringChannelInitializer()` constructor set is maximum packet size. It does that based on configuration object passed as a thord argument, which has to implement `StringChannelInitializer.Configuration` interface. All you need to do is simply implement the `getMaxPacketSize()` that will return your desired packet size limit.
