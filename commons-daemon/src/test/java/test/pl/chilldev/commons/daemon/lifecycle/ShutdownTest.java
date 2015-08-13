/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.apache.commons.daemon.Daemon;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

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

        verify(this.daemon).stop();
        verify(this.daemon).destroy();
    }

    @Test
    public void runWithException()
        throws
            Exception
    {
        Exception error = new Exception("error");

        doThrow(error).when(this.daemon).stop();

        Shutdown shutdown = new Shutdown(this.daemon);

        shutdown.run();

        verify(this.daemon).stop();
        verify(this.daemon, never()).destroy();
    }
}
