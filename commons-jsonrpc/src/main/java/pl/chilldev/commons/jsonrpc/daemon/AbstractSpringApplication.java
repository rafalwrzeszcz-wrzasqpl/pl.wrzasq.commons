/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.util.Collection;

import org.springframework.beans.factory.BeanFactoryUtils;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base daemon implementation based on Spring context.
 */
public abstract class AbstractSpringApplication extends AbstractApplication
{
    /**
     * Application context environment.
     */
    protected AnnotationConfigApplicationContext context;

    /**
     * Runs all listeners.
     */
    @Override
    public void start()
    {
        // load fresh context
        this.logger.info("Loading application context.");
        this.context = new AnnotationConfigApplicationContext();
        this.context.scan(this.getPackageToScan());
        this.context.refresh();

        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        super.stop();

        // clean up the context
        if (this.context != null) {
            this.context.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Listener> buildListeners()
    {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(
            this.context,
            Listener.class
        ).values();
    }

    /**
     * Returns base package name for context scanning.
     *
     * @return Package name that contains any Spring configuration.
     */
    protected abstract String getPackageToScan();
}
