/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.daemon;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;
import pl.chilldev.commons.web.context.WebApplicationContextLoader;
import pl.chilldev.commons.web.daemon.AbstractSpringWebDaemon;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSpringWebDaemonTest extends AbstractSpringWebDaemon
{
    @Spy
    private AbstractWebDaemonTest.TestServer server = new AbstractWebDaemonTest.TestServer();

    @Mock
    private WebApplicationContextLoader mockContextLoader;

    @Mock
    private WebApplicationContext applicationContext;

    @Before
    public void setUp()
    {
        Mockito.when(this.mockContextLoader.initWebApplicationContext(Mockito.isA(ServletContext.class)))
            .thenReturn(this.applicationContext);
        Mockito.when(this.applicationContext.getBean(Server.class))
            .thenReturn(this.server);
    }

    @Test
    public void createServletContextTest()
    {
        ServletContextHandler servlet = this.createServletContext();

        Assert.assertNotNull(
            "AbstractSpringWebDaemon.createServletContext() should assign logger to context instance.",
            servlet.getLogger()
        );
        Assert.assertEquals(
            "AbstractSpringWebDaemon.createServletContext() should set root path to the servlet context.",
            "/",
            servlet.getContextPath()
        );
    }

    @Test
    public void createServerTest()
    {
        Server server = this.createServer();

        Assert.assertSame(
            "AbstractSpringWebDaemon.createServer() should return Jetty server instance acquired from Spring DIC.",
            this.server,
            server
        );

        Mockito.verify(this.mockContextLoader).initWebApplicationContext(Mockito.isA(ServletContext.class));
        Mockito.verify(this.applicationContext).getBean("called!");
    }

    @Test
    public void stopServerRunning()
    {
        this.createServer();
        this.stopServer();

        Mockito.verify(this.mockContextLoader).closeWebApplicationContext();
    }

    @Test
    public void stopServerNotRunning()
    {
        this.stopServer();

        Mockito.verify(this.mockContextLoader, Mockito.never()).closeWebApplicationContext();
    }

    @Override
    protected WebApplicationContextLoader createContextLoader()
    {
        return this.mockContextLoader;
    }

    @Override
    protected void configureServletContext(ServletContextHandler servlet, BeanFactory beanFactory)
    {
        beanFactory.getBean("called!");
        // dummy method
    }
}
