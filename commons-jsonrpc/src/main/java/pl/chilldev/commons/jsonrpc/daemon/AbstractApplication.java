/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonUserSignal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base daemon application class.
 */
public abstract class AbstractApplication
    implements
        Daemon,
        DaemonUserSignal
{
    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(AbstractApplication.class);

    /**
     * List of children threads.
     */
    protected Collection<Listener> threads = new HashSet<>();

    /**
     * Runs all listeners.
     */
    @Override
    public void start()
    {
        // running threads
        for (Listener thread : this.buildListeners()) {
            try {
                thread.start();
                this.threads.add(thread);
            //CHECKSTYLE:OFF: IllegalCatchCheck
            } catch (Throwable error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
                this.logger.error(
                    "Not starting \"{}\" because of {}.",
                    thread.getName(),
                    error.getMessage(),
                    error
                );
            }
        }

        this.logger.trace("Working.");
    }

    /**
     * Stops listeners.
     */
    @Override
    public void stop()
    {
        this.logger.info("Stopping…");

        // stop all threads
        this.threads.forEach(Listener::release);

        // wait for all threads
        for (Thread thread : this.threads) {
            try {
                // this is to make sure that thread don't hang on some I/O
                thread.interrupt();
                thread.join();
            } catch (InterruptedException error) {
                this.logger.error("Had to interrupt while waiting for thread \"{}\".", thread.getName(), error);
            }
        }
        // just to clean references
        this.threads.clear();
    }

    /**
     * Just re-loads configuration.
     */
    @Override
    public void signal()
    {
        this.logger.info("Handling configuration reload.");

        // no deep in-sight for now, but at least JVM bootstrapping is avoided

        this.stop();
        this.start();
    }

    /**
     * Daemon resources initialization.
     *
     * @param context Runtime context for daemon.
     */
    @Override
    public void init(DaemonContext context)
    {
        // diagnostic info
        this.logger.info(
            "Started {} daemon v.{} on {} {} ({}) / Java {} ({}).",
            new Object[] {
                this.getDaemonName(),
                this.getDaemonVersion(),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
            }
        );
    }

    /**
     * Daemon resources freeing.
     */
    @Override
    public void destroy()
    {
        // dummy method; not used - needed by org.apache.commons.daemon.Daemon
    }

    /**
     * Returns daemon application name.
     *
     * @return String representation for information purpose.
     */
    protected abstract String getDaemonName();

    /**
     * Returns daemon application version.
     *
     * @return String representation for information purpose.
     */
    protected abstract String getDaemonVersion();

    /**
     * Retruns collection of listeners (not started, but instantiated).
     *
     * @return Collection of listeners to be started by the daemon.
     */
    protected abstract Collection<Listener> buildListeners();
}
