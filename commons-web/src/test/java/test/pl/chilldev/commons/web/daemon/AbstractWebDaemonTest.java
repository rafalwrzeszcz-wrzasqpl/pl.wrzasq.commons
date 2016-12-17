/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.daemon;

import org.eclipse.jetty.server.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import pl.chilldev.commons.web.daemon.AbstractWebDaemon;

@RunWith(MockitoJUnitRunner.class)
public class AbstractWebDaemonTest
{
    private static class TestServer extends Server
    {
        @Override
        public void doStart()
        {
            // dummy method
        }
    }

    @Spy
    private TestServer server = new AbstractWebDaemonTest.TestServer();

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
        Mockito.doThrow(Exception.class).when(this.server).doStart();

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
