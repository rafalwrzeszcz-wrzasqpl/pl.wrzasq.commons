/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.context;

import java.lang.reflect.Field;
import java.util.Collections;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.WebApplicationContext;
import pl.chilldev.commons.web.context.WebApplicationContextLoader;

@RunWith(MockitoJUnitRunner.class)
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

    @Mock
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp()
    {
        Mockito.when(this.servletContext.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        Mockito.when(this.servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
        Mockito.when(this.webApplicationContext.getServletContext()).thenReturn(null);
    }

    @Test
    public void initWebApplicationContext()
    {
        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        WebApplicationContext applicationContext = contextLoader.initWebApplicationContext(this.servletContext);

        try {
            Assert.assertArrayEquals(
                "WebApplicationContextLoader.createWebApplicationContext() should set active profiles list.",
                new String[] {"test", "foo"},
                applicationContext.getEnvironment().getActiveProfiles()
            );
            Assert.assertSame(
                "WebApplicationContextLoader.createWebApplicationContext() should register configuration class.",
                WebApplicationContextLoaderTest.BEAN,
                applicationContext.getBean(WebApplicationContextLoaderTest.class)
            );
        } finally {
            contextLoader.closeWebApplicationContext();
        }
    }

    @Test
    public void closeWebApplicationContext()
    {
        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        contextLoader.initWebApplicationContext(this.servletContext);

        contextLoader.closeWebApplicationContext();

        Mockito
            .verify(this.servletContext)
            .removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    @Test
    public void closeWebApplicationContextWithNullServletContent()
        throws IllegalAccessException, NoSuchFieldException
    {
        WebApplicationContextLoader contextLoader = this.createWebApplicationContextLoader();

        Field field = contextLoader.getClass().getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(contextLoader, this.webApplicationContext);

        contextLoader.closeWebApplicationContext();
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
