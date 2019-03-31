/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.web.daemon;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.BeanFactory;
import pl.wrzasq.commons.web.context.WebApplicationContextLoader;

/**
 * Base class for Spring-based web application daemons.
 */
public abstract class AbstractSpringWebDaemon extends AbstractWebDaemon {
    /**
     * Spring application synchronizer.
     */
    private WebApplicationContextLoader contextLoader;

    /**
     * Creates Spring context loader for the application.
     *
     * @return Spring context loader.
     */
    protected abstract WebApplicationContextLoader createContextLoader();

    /**
     * Creates a servlet context for the application.
     *
     * @return Servlet context handler.
     */
    protected ServletContextHandler createServletContext() {
        // servlet context initialization
        ServletContextHandler servlet = new ServletContextHandler();
        servlet.setLogger(Log.getLogger(AbstractWebDaemon.ROOT_CONTEXT_PATH));
        servlet.setContextPath(AbstractWebDaemon.ROOT_CONTEXT_PATH);
        return servlet;
    }

    /**
     * Customization hook for setting servlet.
     *
     * <p>
     * This hook serves mainly purpose to configure servlet context. Everything else should be handled within Spring
     * context.
     * </p>
     *
     * @param servlet Application servlet context.
     * @param beanFactory Spring context DIC.
     */
    protected abstract void configureServletContext(ServletContextHandler servlet, BeanFactory beanFactory);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Server createServer() {
        // initialize all of the resources
        this.contextLoader = this.createContextLoader();
        ServletContextHandler servlet = this.createServletContext();
        BeanFactory beanFactory = this.contextLoader.initWebApplicationContext(servlet.getServletContext());

        // apply custom hook
        this.configureServletContext(servlet, beanFactory);

        // run the server with the application
        Server server = beanFactory.getBean(Server.class);
        server.setHandler(servlet);
        return server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopServer() {
        if (this.contextLoader != null) {
            this.contextLoader.closeWebApplicationContext();
        }
    }
}
