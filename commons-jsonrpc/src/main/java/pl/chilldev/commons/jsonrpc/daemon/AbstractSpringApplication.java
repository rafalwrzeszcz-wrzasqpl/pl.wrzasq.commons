/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.BeanFactoryUtils;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base daemon implementation based on Spring context.
 */
public abstract class AbstractSpringApplication extends AbstractApplication
{
    /**
     * Configures the application before it starts.
     *
     * <p>
     * Defining beans of this type allows one to customize application behavior.
     * </p>
     */
    @FunctionalInterface
    public interface ApplicationConfigurer
    {
        /**
         * Configures application properties.
         *
         * @param application Subject application.
         */
        void configureApplication(AbstractApplication application);
    }

    /**
     * Application context environment.
     */
    private AnnotationConfigApplicationContext context;

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

        // configures current application
        for (AbstractSpringApplication.ApplicationConfigurer configurer
            : BeanFactoryUtils.beansOfTypeIncludingAncestors(
                this.context,
                AbstractSpringApplication.ApplicationConfigurer.class
            ).values()
        )
        {
            configurer.configureApplication(this);
        }

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
    protected Collection<Listener<?>> buildListeners()
    {
        Collection<Listener<?>> listeners = new ArrayList<>();

        // this is to cast raw types into generics
        BeanFactoryUtils.beansOfTypeIncludingAncestors(
            this.context,
            Listener.class
        ).values().forEach(listeners::add);

        return listeners;
    }

    /**
     * Returns base package name for context scanning.
     *
     * @return Package name that contains any Spring configuration.
     */
    protected String getPackageToScan()
    {
        return this.getClass().getPackage().getName();
    }
}
