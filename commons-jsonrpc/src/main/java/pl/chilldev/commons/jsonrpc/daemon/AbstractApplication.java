/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.util.Collection;
import java.util.HashSet;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

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
    private Collection<Listener<?>> listeners = new HashSet<>();

    /**
     * Acceptor threads.
     */
    private EventLoopGroup acceptors;

    /**
     * Worker threads.
     */
    private EventLoopGroup workers;

    /**
     * Acceptors thread count.
     */
    private int acceptorsCount;

    /**
     * Workers thread count.
     */
    private int workersCount;

    /**
     * Sets acceptors threads count.
     *
     * @param acceptorsCount Threads count.
     */
    public void setAcceptorsCount(int acceptorsCount)
    {
        this.acceptorsCount = acceptorsCount;
    }

    /**
     * Sets workers threads count.
     *
     * @param workersCount Threads count.
     */
    public void setWorkersCount(int workersCount)
    {
        this.workersCount = workersCount;
    }

    /**
     * Runs all listeners.
     */
    @Override
    public void start()
    {
        this.acceptors = new NioEventLoopGroup(this.acceptorsCount);
        this.workers = new NioEventLoopGroup(this.workersCount);

        // running threads
        for (Listener<?> listener : this.buildListeners()) {
            try {
                listener.start(this.acceptors, this.workers);
                this.listeners.add(listener);
                //CHECKSTYLE:OFF: IllegalCatchCheck
            } catch (Throwable error) {
                //CHECKSTYLE:ON: IllegalCatchCheck
                this.logger.error(
                    "Not starting \"{}\" because of {}.",
                    listener.getName(),
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

        // wait for all listener
        for (Listener<?> listener : this.listeners) {
            try {
                listener.stop();
            } catch (InterruptedException error) {
                this.logger.error("Had to interrupt while waiting for thread \"{}\".", listener.getName(), error);
            }
        }
        // just to clean references
        this.listeners.clear();

        // close thread pools
        if (this.acceptors != null) {
            this.acceptors.shutdownGracefully();
        }
        if (this.workers != null) {
            this.workers.shutdownGracefully();
        }
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
     * Returns collection of listeners (not started, but instantiated).
     *
     * @return Collection of listeners to be started by the daemon.
     */
    protected abstract Collection<Listener<?>> buildListeners();
}
