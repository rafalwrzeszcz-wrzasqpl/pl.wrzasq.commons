/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon.lifecycle;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.daemon.Daemon;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
        Assert.assertEquals(
            "ChillDevApplication.run() should return 0 if application finish successfully.",
            0,
            new Runner().run(this.daemon)
        );

        Mockito.verify(this.daemon).init(null);
        Mockito.verify(this.daemon).start();
    }

    @Test
    public void runWithException()
        throws
            Exception
    {
        Mockito.doThrow(new RuntimeException("test")).when(this.daemon).init(null);

        Assert.assertEquals(
            "ChillDevApplication.run() should return -1 if error occured during application run.",
            -1,
            new Runner().run(this.daemon)
        );

        Mockito.verify(this.daemon, Mockito.never()).start();
    }
}
