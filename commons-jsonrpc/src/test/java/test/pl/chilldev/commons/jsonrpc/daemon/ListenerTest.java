/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import java.net.InetSocketAddress;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import org.apache.mina.util.AvailablePortFinder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.daemon.Listener;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class ListenerTest
{
    private static EventLoopGroup acceptors = new NioEventLoopGroup();

    private static EventLoopGroup workers = new NioEventLoopGroup();

    @AfterClass
    public static void tearDown()
    {
        ListenerTest.acceptors.shutdownGracefully();
        ListenerTest.workers.shutdownGracefully();
    }

    @Test
    public void setMaxPacketSize()
    {
        int maxPacketSize = 1024;

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setMaxPacketSize(maxPacketSize);

        Assert.assertEquals(
            "Listener.setMaxPacketSize() should set maximum size of JSON-RPC packet.",
            maxPacketSize,
            listener.getMaxPacketSize()
        );
    }

    @Test
    public void getName()
    {
        String name = "test";

        Listener<ContextInterface> listener = new Listener<>(name, null, new Dispatcher<ContextInterface>());

        Assert.assertEquals(
            "Listener.getName() should return listener name.",
            name,
            listener.getName()
        );
    }

    @Test
    public void run()
        throws
            InterruptedException
    {
        int port = AvailablePortFinder.getNextAvailable(1024);

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setAddress(new InetSocketAddress("127.0.0.1", port));
        listener.start(ListenerTest.acceptors, ListenerTest.workers);

        Assert.assertFalse("Listener.run() should start socket acceptor on specified port.", AvailablePortFinder.available(port));

        listener.stop();
    }

    @Test
    public void runWithoutAddress()
        throws
            InterruptedException
    {
        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.start(ListenerTest.acceptors, ListenerTest.workers);
    }

    @Test
    public void stop()
        throws
            InterruptedException
    {
        int port = AvailablePortFinder.getNextAvailable(1024);

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setAddress(new InetSocketAddress("127.0.0.1", port));
        listener.start(ListenerTest.acceptors, ListenerTest.workers);
        listener.stop();

        Assert.assertTrue("Listener.stop() should release socket port.", AvailablePortFinder.available(port));
    }

    @Test
    public void stopWithoutChannel()
        throws
            InterruptedException
    {
        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.stop();
    }
}
