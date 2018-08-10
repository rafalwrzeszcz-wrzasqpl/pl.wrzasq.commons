/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 - 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.web.daemon;

import org.eclipse.jetty.server.Server;
import pl.chilldev.commons.daemon.AbstractDaemon;

/**
 * Base class for web application daemons.
 */
public abstract class AbstractWebDaemon extends AbstractDaemon
{
    /**
     * Catch-all servlet context path.
     */
    public static final String ROOT_CONTEXT_PATH = "/";

    /**
     * Filter path specification that matches all requests.
     */
    public static final String PATHSPEC_CATCH_ALL = "/*";

    /**
     * Jetty HTTP server.
     */
    protected Server server;

    /**
     * Creates server instance for current application.
     *
     * @return HTTP server.
     */
    protected abstract Server createServer();

    /**
     * Stops HTTP server assigned with current application.
     */
    protected abstract void stopServer();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {
        // run the server created by the application
        this.server = this.createServer();

        try {
            this.server.start();
            //CHECKSTYLE:OFF: IllegalCatchCheck
        } catch (Exception error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
            this.logger.error(
                "Not starting because of {}.",
                error.getMessage(),
                error
            );
        }

        this.logger.trace("Working.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        this.logger.info("Stopping…");

        try {
            if (this.server != null) {
                this.stopServer();
                this.server.join();
                this.server = null;
            } else {
                this.logger.trace("Server was not running.");
            }
        } catch (InterruptedException error) {
            this.logger.error("Had to interrupt while waiting for Jetty server.", error);
        }
    }
}
