/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.chilldev.commons.daemon.AbstractDaemon;

@ExtendWith(MockitoExtension.class)
public class AbstractDaemonTest
{
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

    @Test
    public void signalThrowsException() throws Exception
    {
        Mockito.doThrow(Exception.class).when(this.daemon).start();
        Assertions.assertThrows(
            Exception.class,
            this.daemon::signal,
            "AbstractDaemon.signal() should throw exception if re-starting daemon fails."
        );
    }
}
