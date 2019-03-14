/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.daemon;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonUserSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic daemon application stub.
 */
public abstract class AbstractDaemon implements Daemon, DaemonUserSignal
{
    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(AbstractDaemon.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void signal()
    {
        this.logger.info("Handling configuration reload.");

        // no deep in-sight for now, but at least JVM bootstrapping is avoided

        try {
            this.stop();
            this.start();
            //CHECKSTYLE:OFF: IllegalCatchCheck
        } catch (Exception error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
            this.logger.error("Failed to handle signal call.", error);
            throw new RuntimeException(error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(DaemonContext context)
    {
        // diagnostic info
        this.logger.info(
            "Started {} daemon v.{} on {} {} ({}) / Java {} ({}).",
            this.getClass().getPackage().getName(),
            Package.DEFAULT_PACKAGE.getVersion(),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"),
            System.getProperty("java.version"),
            System.getProperty("java.vendor")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        // dummy method; not used - needed by org.apache.commons.daemon.Daemon
    }
}
