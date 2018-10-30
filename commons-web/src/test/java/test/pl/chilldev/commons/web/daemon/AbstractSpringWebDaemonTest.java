/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.daemon;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;
import pl.chilldev.commons.web.context.WebApplicationContextLoader;
import pl.chilldev.commons.web.daemon.AbstractSpringWebDaemon;

@ExtendWith(MockitoExtension.class)
public class AbstractSpringWebDaemonTest extends AbstractSpringWebDaemon
{
    private static final String DUMMY_BEAN_ID = "called!";

    @Spy
    private AbstractWebDaemonTest.TestServer server = new AbstractWebDaemonTest.TestServer();

    @Mock
    private WebApplicationContextLoader mockContextLoader;

    @Mock
    private WebApplicationContext applicationContext;

    private void setUpInitialization()
    {
        Mockito.when(this.mockContextLoader.initWebApplicationContext(Mockito.isA(ServletContext.class)))
            .thenReturn(this.applicationContext);
        Mockito.doReturn(this.server)
            .when(this.applicationContext).getBean(Server.class);
        Mockito.doReturn(null)
            .when(this.applicationContext).getBean(AbstractSpringWebDaemonTest.DUMMY_BEAN_ID);
    }

    @Test
    public void createServletContextTest()
    {
        ServletContextHandler servlet = this.createServletContext();

        Assertions.assertNotNull(
            servlet.getLogger(),
            "AbstractSpringWebDaemon.createServletContext() should assign logger to context instance."
        );
        Assertions.assertEquals(
            "/",
            servlet.getContextPath(),
            "AbstractSpringWebDaemon.createServletContext() should set root path to the servlet context."
        );
    }

    @Test
    public void createServerTest()
    {
        this.setUpInitialization();

        Server server = this.createServer();

        Assertions.assertSame(
            this.server,
            server,
            "AbstractSpringWebDaemon.createServer() should return Jetty server instance acquired from Spring DIC."
        );

        Mockito.verify(this.mockContextLoader).initWebApplicationContext(Mockito.isA(ServletContext.class));
        Mockito.verify(this.applicationContext).getBean(AbstractSpringWebDaemonTest.DUMMY_BEAN_ID);
    }

    @Test
    public void stopServerRunning()
    {
        this.setUpInitialization();

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
        beanFactory.getBean(AbstractSpringWebDaemonTest.DUMMY_BEAN_ID);
        // dummy method
    }
}
