/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.daemon.AbstractApplication;
import pl.chilldev.commons.jsonrpc.daemon.Listener;

@RunWith(MockitoJUnitRunner.class)
public class AbstractApplicationTest
{
    public class Application extends AbstractApplication
    {
        private Listener<?> listener;

        public Application(Listener<?> listener)
        {
            this.listener = listener;
        }

        @Override
        protected String getDaemonName()
        {
            return "test.pl.chilldev.commons.jsonrpc.daemon";
        }

        @Override
        protected String getDaemonVersion()
        {
            return "0.0.0";
        }

        @Override
        protected Collection<Listener<?>> buildListeners()
        {
            Collection<Listener<?>> listeners = new HashSet<>();
            listeners.add(this.listener);
            return listeners;
        }
    }

    @Mock
    private Listener<?> listener;

    @Test
    public void start()
        throws
            InterruptedException
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.setAcceptorsCount(1);
        app.setWorkersCount(1);
        app.start();

        Mockito.verify(this.listener).start(Matchers.any(), Matchers.any());

        // just for code coverage
        app.init(null);
        app.destroy();
    }

    @Test
    public void startWithException()
        throws
            InterruptedException
    {
        Mockito.when(this.listener.getName()).thenReturn("test");
        Mockito.doThrow(InterruptedException.class).when(this.listener).start(Matchers.any(), Matchers.any());

        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
    }

    @Test
    public void stop()
        throws
            InterruptedException
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
        app.stop();

        Mockito.verify(this.listener).stop();
    }

    @Test
    public void stopWithInterruptedException()
        throws
            InterruptedException
    {
        Mockito.when(this.listener.getName()).thenReturn("test");
        Mockito.when(this.listener.stop()).thenThrow(InterruptedException.class);

        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
        app.stop();
    }

    @Test
    public void signal()
        throws
            InterruptedException
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
        app.signal();

        Mockito.verify(this.listener, Mockito.times(2)).start(Matchers.any(), Matchers.any());
        Mockito.verify(this.listener).stop();
    }
}
