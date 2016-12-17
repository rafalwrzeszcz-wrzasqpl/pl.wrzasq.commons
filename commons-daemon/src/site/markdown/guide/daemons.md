<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
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
