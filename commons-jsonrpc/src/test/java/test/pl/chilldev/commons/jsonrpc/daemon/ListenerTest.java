/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import java.net.InetSocketAddress;

import org.apache.mina.util.AvailablePortFinder;

import org.junit.Test;
import static org.junit.Assert.*;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.daemon.Listener;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

public class ListenerTest
{
    @Test
    public void setMaxPacketSize()
    {
        int maxPacketSize = 1024;

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setMaxPacketSize(maxPacketSize);
        listener.setSleepTick(100); // just for code coverage :(

        assertEquals(
            "Listener.setMaxPacketSize() should set maximum size of JSON-RPC packet.",
            maxPacketSize,
            listener.getMaxPacketSize()
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
        listener.start();

        // this can take a while - any better way?
        Thread.sleep(500);

        assertTrue("Listener.run() should start the listener thread.", listener.isAlive());
        assertFalse("Listener.run() should start socket acceptor on specified port.", AvailablePortFinder.available(port));

        listener.interrupt();
        listener.join();

        assertFalse("Listener.run() should stop the listener thread, when interrupted.", listener.isAlive());
        assertTrue("Listener.run() should release socket port, when interrupted.", AvailablePortFinder.available(port));
    }

    @Test
    public void runIOException()
        throws
            InterruptedException
    {
        int port = AvailablePortFinder.getNextAvailable(1024);

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setAddress(new InetSocketAddress("127.0.0.1", port));
        listener.start();

        // this can take a while - any better way?
        Thread.sleep(500);

        Listener<ContextInterface> listenerBusy = new Listener<>("test-busy", null, new Dispatcher<ContextInterface>());
        listenerBusy.setAddress(new InetSocketAddress("127.0.0.1", port));
        listenerBusy.start();

        // this can take a while - any better way?
        Thread.sleep(500);

        assertTrue("Listener.run() should start the listener thread.", listener.isAlive());
        assertFalse("Listener.run() should start socket acceptor on specified port.", AvailablePortFinder.available(port));
        assertFalse("Listener.run() should fail if address is already in use.", listenerBusy.isAlive());

        listener.interrupt();
        listener.join();
        // this is just for sure
        listenerBusy.interrupt();
        listenerBusy.join();

        assertFalse("Listener.run() should stop the listener thread, when interrupted.", listener.isAlive());
        assertTrue("Listener.run() should release socket port, when interrupted.", AvailablePortFinder.available(port));
    }

    @Test
    public void runWithoutAddress()
    {
        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.run();

        assertFalse(
            "Listener.run() should not run the thread if the listen address is not configured.",
            listener.isAlive()
        );
    }

    @Test
    public void release()
        throws
            InterruptedException
    {
        int port = AvailablePortFinder.getNextAvailable(1024);

        Listener<ContextInterface> listener = new Listener<>("test", null, new Dispatcher<ContextInterface>());
        listener.setAddress(new InetSocketAddress("127.0.0.1", port));
        listener.start();

        // this can take a while - any better way?
        Thread.sleep(500);

        assertTrue("Listener.run() should start the listener thread.", listener.isAlive());

        listener.release();
        Thread.sleep(500);

        assertFalse("Listener.release() should stop the listener thread.", listener.isAlive());
        assertTrue("Listener.release() should release socket port.", AvailablePortFinder.available(port));
    }
}
