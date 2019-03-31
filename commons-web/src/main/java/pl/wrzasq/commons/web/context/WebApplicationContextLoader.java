/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.web.context;

import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Initializes web application applicationContext.
 */
public class WebApplicationContextLoader extends ContextLoader {
    /**
     * Application configuration class.
     */
    private Class<?> configurationClass;

    /**
     * Spring profiles.
     */
    private String[] profiles;

    /**
     * Web application applicationContext.
     */
    private WebApplicationContext applicationContext;

    /**
     * Initializes applicationContext loader with given configuration.
     *
     * @param configurationClass Configuration class to be loaded on startup.
     * @param profiles List of Spring profiles to activate.
     */
    public WebApplicationContextLoader(Class<?> configurationClass, String... profiles) {
        this.configurationClass = configurationClass;
        this.profiles = profiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebApplicationContext createWebApplicationContext(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.getEnvironment().setActiveProfiles(this.profiles);
        applicationContext.register(this.configurationClass);
        return applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
        return this.applicationContext = super.initWebApplicationContext(servletContext);
    }

    /**
     * Closes applicationContext of previously created web application.
     */
    public void closeWebApplicationContext() {
        Optional.ofNullable(this.applicationContext)
            .map(WebApplicationContext::getServletContext)
            .ifPresent(this::closeWebApplicationContext);
    }
}
