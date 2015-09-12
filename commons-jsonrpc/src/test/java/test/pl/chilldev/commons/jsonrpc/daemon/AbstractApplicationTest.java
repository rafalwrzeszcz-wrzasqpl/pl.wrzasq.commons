/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.daemon.AbstractApplication;
import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.daemon.Listener;

@RunWith(MockitoJUnitRunner.class)
public class AbstractApplicationTest
{
    public class TestListener extends Listener<ContextInterface>
    {
        protected boolean fail;

        public TestListener(String name, boolean fail)
        {
            super(name, null, null);
            this.fail = fail;
        }

        public TestListener(String name)
        {
            this(name, true);
        }

        @Override
        public void start()
        {
            if (this.fail) {
                throw new IllegalThreadStateException("error");
            }
            super.start();
        }

        @Override
        public void run()
        {
            try {
                this.noop();
            } catch (InterruptedException error) {
                try {
                    this.noop();
                } catch (InterruptedException silence) {
                }
            }
        }

        public void noop()
            throws
                InterruptedException
        {
            while (true) {
                Thread.sleep(500);
            }
        }
    }

    public class Application extends AbstractApplication
    {
        protected Listener thread;

        public Application(Listener thread)
        {
            this.thread = thread;
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
        protected Collection<Listener> buildListeners()
        {
            Collection<Listener> threads = new HashSet<>();
            threads.add(this.thread);
            return threads;
        }
    }

    @Mock
    protected Listener listener;

    @Test
    public void start()
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();

        Mockito.verify(this.listener).start();

        // just for code coverage
        app.init(null);
        app.destroy();
    }

    @Test
    public void startWithException()
    {
        // impossible to mock, Thread.getName() is a final method
        AbstractApplicationTest.TestListener listener = new AbstractApplicationTest.TestListener(
            "test"
        );
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(listener);

        app.start();
    }

    @Test
    public void stop()
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
        app.stop();

        Mockito.verify(this.listener).release();
    }

    @Test
    public void stopWithInterruptedException()
    {
        // impossible to mock, Thread.getName() and Thread.join() are final methods
        AbstractApplicationTest.TestListener listener = new AbstractApplicationTest.TestListener(
            "test",
            false
        );
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(listener);
        final Thread thread = Thread.currentThread();
        Thread closer = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException error) {
                    System.exit(1);
                }
                thread.interrupt();
            }
        });

        app.start();
        closer.start();
        app.stop();

        // second interrupt is needed to close fake listener thread
        listener.interrupt();
    }

    @Test
    public void signal()
    {
        AbstractApplicationTest.Application app = new AbstractApplicationTest.Application(this.listener);

        app.start();
        app.signal();

        Mockito.verify(this.listener, Mockito.times(2)).start();
        Mockito.verify(this.listener).release();
    }
}
