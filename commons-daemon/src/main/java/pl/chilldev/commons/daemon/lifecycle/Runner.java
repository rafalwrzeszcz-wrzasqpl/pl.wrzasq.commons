/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.daemon.lifecycle;

// dependencies and sub-modules
import org.apache.commons.daemon.Daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles application shutdown.
 */
public class Runner
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(Runner.class);

    /**
     * Runs application.
     *
     * <p>
     * This method runs application directly by invoking Apache Commons Daemon flow.
     * </p>
     *
     * <p>
     * Standard usage is just `(new Runner()).run(new ChillDevApplication())`.
     * </p>
     *
     * @param daemon Daemon application.
     */
    public void run(Daemon daemon)
    {
        try {
            // perform initialization and start daemon
            daemon.init(null);
            daemon.start();

            // schedule shutdown hook for daemon cleanup
            Runtime.getRuntime().addShutdownHook(new Shutdown(daemon));
            //CHECKSTYLE:OFF: IllegalCatchCheck
        } catch (Exception error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
            this.logger.error("Fatal error {}.", error.getMessage(), error);

            throw new RuntimeException(
                String.format("Fatal error: %s", error.getMessage()),
                error
            );
        }
    }
}
