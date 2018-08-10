/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.daemon.AbstractDaemon;

public class AbstractDaemonTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Spy
    private AbstractDaemon daemon = new AbstractDaemon()
    {
        @Override
        public void start() throws Exception
        {
            // dummy method
        }

        @Override
        public void stop() throws Exception
        {
            // dummy method
        }
    };

    @Test
    public void signal() throws Exception
    {
        // just for code coverate
        this.daemon.init(null);
        this.daemon.destroy();

        this.daemon.signal();

        Mockito.verify(this.daemon).start();
        Mockito.verify(this.daemon).stop();
    }

    @Test(expected = RuntimeException.class)
    public void signalThrowsException() throws Exception
    {
        Mockito.doThrow(Exception.class).when(this.daemon).start();
        this.daemon.signal();
    }
}
