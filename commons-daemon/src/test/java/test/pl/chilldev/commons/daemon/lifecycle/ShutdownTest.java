/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon.lifecycle;

import org.apache.commons.daemon.Daemon;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import pl.chilldev.commons.daemon.lifecycle.Shutdown;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownTest
{
    @Mock
    private Daemon daemon;

    @Test
    public void run()
        throws
            Exception
    {
        Shutdown shutdown = new Shutdown(this.daemon);

        shutdown.run();

        Mockito.verify(this.daemon).stop();
        Mockito.verify(this.daemon).destroy();
    }

    @Test
    public void runWithException()
        throws
            Exception
    {
        Exception error = new Exception("error");

        Mockito.doThrow(error).when(this.daemon).stop();

        Shutdown shutdown = new Shutdown(this.daemon);

        shutdown.run();

        Mockito.verify(this.daemon).stop();
        Mockito.verify(this.daemon, Mockito.never()).destroy();
    }
}
