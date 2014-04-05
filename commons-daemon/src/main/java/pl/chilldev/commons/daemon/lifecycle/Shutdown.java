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

package pl.chilldev.commons.daemon.lifecycle;

// dependencies and sub-modules
import org.apache.commons.daemon.Daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.exception.ExceptionFormatter;

/**
 * Handles application shutdown.
 *
 * @version 0.0.2
 * @since 0.0.2
 */
public class Shutdown extends Thread
{
    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(Shutdown.class);

    /**
     * Application service.
     */
    protected Daemon daemon;

    /**
     * Exception message formatter.
     */
    protected ExceptionFormatter exceptionFormatter = ExceptionFormatter.SIMPLE_FORMAT;

    /**
     * Initializes reference.
     *
     * @param daemon Running application.
     * @since 0.0.2
     */
    public Shutdown(Daemon daemon)
    {
        this.daemon = daemon;
    }

    /**
     * Sets exception formatter.
     *
     * @param exceptionFormatter New exception message formatter.
     * @since 0.0.2
     */
    public void setExceptionFormatter(ExceptionFormatter exceptionFormatter)
    {
        this.exceptionFormatter = exceptionFormatter;
    }

    /**
     * Shut application down.
     *
     * @since 0.0.2
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
            this.logger.error("Error while stopping dameon {}.", this.exceptionFormatter.format(error), error);
        }
    }
}
