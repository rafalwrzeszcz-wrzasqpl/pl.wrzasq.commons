/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.daemon.lifecycle;

// dependencies and sub-modules
import org.apache.commons.daemon.Daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles application shutdown.
 */
public class Shutdown extends Thread
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(Shutdown.class);

    /**
     * Application service.
     */
    private Daemon daemon;

    /**
     * Initializes reference.
     *
     * @param daemon Running application.
     */
    public Shutdown(Daemon daemon)
    {
        this.daemon = daemon;
    }

    /**
     * Shut application down.
     */
    @Override
    public void run()
    {
        try {
            this.daemon.stop();
            this.daemon.destroy();
            this.logger.trace("Good bye!");
            //CHECKSTYLE:OFF: IllegalCatchCheck
        } catch (Throwable error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
            this.logger.error("Error while stopping dameon {}.", error.getMessage(), error);
        }
    }
}
