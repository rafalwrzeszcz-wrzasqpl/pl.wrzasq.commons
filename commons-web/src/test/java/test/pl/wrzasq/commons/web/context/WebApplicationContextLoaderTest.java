/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.web.context;

import java.util.Collections;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.WebApplicationContext;
import pl.wrzasq.commons.web.context.WebApplicationContextLoader;

@ExtendWith(MockitoExtension.class)
public class WebApplicationContextLoaderTest
{
    // needed just for identity check
    private static final WebApplicationContextLoaderTest BEAN = new WebApplicationContextLoaderTest();

    @Bean
    @Primary
    public WebApplicationContextLoaderTest bean()
    {
        return WebApplicationContextLoaderTest.BEAN;
    }

    @Mock
    private ServletContext servletContext;

    private void setUpAttributes()
    {
        Mockito.when(this.servletContext.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        Mockito.when(this.servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
    }

    @Test
    public void initWebApplicationContext()
    {
        this.setUpAttributes();

        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        WebApplicationContext applicationContext = contextLoader.initWebApplicationContext(this.servletContext);

        try {
            Assertions.assertArrayEquals(
                new String[] {"test", "foo"},
                applicationContext.getEnvironment().getActiveProfiles(),
                "WebApplicationContextLoader.createWebApplicationContext() should set active profiles list."
            );
            Assertions.assertSame(
                WebApplicationContextLoaderTest.BEAN,
                applicationContext.getBean(WebApplicationContextLoaderTest.class),
                "WebApplicationContextLoader.createWebApplicationContext() should register configuration class."
            );
        } finally {
            contextLoader.closeWebApplicationContext();
        }
    }

    @Test
    public void closeWebApplicationContext()
    {
        this.setUpAttributes();

        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        contextLoader.initWebApplicationContext(this.servletContext);

        contextLoader.closeWebApplicationContext();

        Mockito
            .verify(this.servletContext)
            .removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    @Test
    public void closeWebApplicationContextNotRunning()
    {
        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        contextLoader.closeWebApplicationContext();

        Mockito
            .verify(this.servletContext, Mockito.never())
            .removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    private WebApplicationContextLoader createWebApplicationContextLoader()
    {
        return new WebApplicationContextLoader(this.getClass(), "test", "foo");
    }
}
