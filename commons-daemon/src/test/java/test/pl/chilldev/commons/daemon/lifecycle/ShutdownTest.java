/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @author Rafał Wrzeszcz <rafal.wrzeszcz@wrzasq.pl>
 * @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
 * @version 0.0.2
 * @since 0.0.2
 * @category ChillDev-Commons
 * @subcategory Daemon
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
import pl.chilldev.commons.exception.ExceptionFormatter;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownTest
{
    @Mock
    private Daemon daemon;

    @Mock
    private ExceptionFormatter exceptionFormatter;

    private Shutdown shutdown;

    @Before
    public void setUp()
    {
        this.shutdown = new Shutdown(this.daemon);
        this.shutdown.setExceptionFormatter(this.exceptionFormatter);
    }

    @Test
    public void run()
        throws
            Exception
    {
        this.shutdown.run();

        verify(this.exceptionFormatter, never()).format(any(Throwable.class));
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

        this.shutdown.run();

        verify(this.exceptionFormatter).format(error);
        verify(this.daemon).stop();
        verify(this.daemon, never()).destroy();
    }
}
