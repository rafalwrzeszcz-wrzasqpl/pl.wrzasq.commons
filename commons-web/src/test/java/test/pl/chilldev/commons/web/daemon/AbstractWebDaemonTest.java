/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.daemon;

import org.eclipse.jetty.server.Server;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.web.daemon.AbstractWebDaemon;

public class AbstractWebDaemonTest
{
    protected static class TestServer extends Server
    {
        @Override
        public void doStart()
        {
            // dummy method
        }
    }

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Spy
    private AbstractWebDaemonTest.TestServer server = new AbstractWebDaemonTest.TestServer();

    @Spy
    private AbstractWebDaemon daemon = new AbstractWebDaemon()
    {
        @Override
        protected Server createServer()
        {
            return AbstractWebDaemonTest.this.server;
        }

        @Override
        protected void stopServer()
        {
            // dummy method
        }
    };

    @Test
    public void start() throws Exception
    {
        this.daemon.start();

        Mockito.verify(this.server).doStart();
    }

    @Test
    public void startWithException() throws Exception
    {
        Mockito.doThrow(RuntimeException.class).when(this.server).doStart();

        this.daemon.start();
    }

    @Test
    public void stop() throws InterruptedException
    {
        this.daemon.start();
        this.daemon.stop();

        Mockito.verify(this.server).join();
    }

    @Test
    public void stopWithoutServer() throws InterruptedException
    {
        this.daemon.stop();

        Mockito.verify(this.server, Mockito.never()).join();
    }

    @Test
    public void stopWithInterruptedException() throws InterruptedException
    {
        this.daemon.start();

        Mockito.doThrow(InterruptedException.class).when(this.server).join();

        this.daemon.stop();
    }
}
