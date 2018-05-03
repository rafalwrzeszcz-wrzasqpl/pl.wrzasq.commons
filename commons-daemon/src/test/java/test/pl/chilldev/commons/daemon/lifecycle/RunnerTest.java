/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon.lifecycle;

import org.apache.commons.daemon.Daemon;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.daemon.lifecycle.Runner;

public class RunnerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

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
