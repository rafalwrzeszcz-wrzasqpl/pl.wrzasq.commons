/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon.lifecycle;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.daemon.Daemon;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import pl.chilldev.commons.daemon.lifecycle.Runner;

@RunWith(MockitoJUnitRunner.class)
public class RunnerTest
{
    @Mock
    private Daemon daemon;

    @Test
    public void run()
        throws
            Exception
    {
        new Runner().run(this.daemon);

        Mockito.verify(this.daemon).init(null);
        Mockito.verify(this.daemon).start();
    }

    @Test(expected = RuntimeException.class)
    public void runWithException()
        throws
            Exception
    {
        Mockito.doThrow(new RuntimeException("test")).when(this.daemon).init(null);

        new Runner().run(this.daemon);
    }
}
