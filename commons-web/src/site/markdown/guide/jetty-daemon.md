<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 - 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Daemon handler for Jetty server

On top of `commons-daemon`, `commons-web` provides base class for running **Jetty** HTTP server as a daemon. It implements `start()` and `stop()` methods of a daemon leaving for you just server instance creation process:

```java
import org.eclipse.jetty.server.Server;

import pl.chilldev.commons.web.daemon.AbstractWebDaemon;

public class MyWebDaemon extends AbstractWebDaemon
{
    @Override
    protected Server createServer();
    {
        Server server;
        /* create and set up server */
        return server;
    }

    @Override
    protected void stopServer();
    {
        this.server.stop();
    }
}
```

## Spring ContextLoader

Another aspect is **Spring** application context loading. To bind it with servlet initialization Spring provides `ContextLoader` interfaces. `commons-web` comes with it's simple implementation as `pl.chilldev.commons.web.context.WebApplicationContextLoader`. To continue with previous example we can go further and manage our application resources (including `Server` instance) through **Spring**:

```java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.springframework.beans.factory.BeanFactory;

import pl.chilldev.commons.web.context.WebApplicationContextLoader;
import pl.chilldev.commons.web.daemon.AbstractWebDaemon;

public class MyWebDaemon extends AbstractWebDaemon
{
    private WebApplicationContextLoader contextLoader;

    @Override
    protected Server createServer();
    {
        // servlet context initialization
        ServletContextHandler servlet = new ServletContextHandler(ServletContextHandler.SESSIONS);

        // populate servlet context into Spring application context
        this.contextLoader = new WebApplicationContextLoader(YourConfigurationClass.class);
        BeanFactory beanFactory = this.contextLoader.initWebApplicationContext(servlet.getServletContext());

        /* set up servlet handler */

        // run the server with the application
        Server server = beanFactory.getBean(Server.class);
        server.setHandler(servlet);
        return server;
    }

    @Override
    protected void stopServer();
    {
        // server can be managed by Spring with it's `.stop()` method as destroy method
        this.contextLoader.closeWebApplicationContext();
    }
}
```
