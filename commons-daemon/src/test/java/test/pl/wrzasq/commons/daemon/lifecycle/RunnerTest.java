/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.daemon.lifecycle;

import org.apache.commons.daemon.Daemon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.daemon.lifecycle.Runner;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void runWithException()
        throws
            Exception
    {
        Mockito.doThrow(new RuntimeException("test")).when(this.daemon).init(null);

        Assertions.assertThrows(
            RuntimeException.class,
            () -> new Runner().run(this.daemon),
            "Runner.run() should throw exception if underlying daemon initialization fails."
        );
    }
}
