<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Shutdown

*Apache Commons Daemon* provides great framework for building native (not only *UNIX*) services with *Java*. However, as the daemons written with them have certain flow, in order to run daemon service thread directly from your application you need to take care about finalization flow (shutdown) - to automate this task, you can use `pl.wrzasq.commons.daemon.lifecycle.Shutdown` class.

It's a thread that you can just run manually, but then it's not so bit benefit from using it, as you can shutdown your service thread by hand also. As a thread it can be attached to current runtime environment as shutdown task for current VM and will be executed automatically when your application will be terminated.

```java
import org.apache.commons.daemon.Daemon;

import pl.wrzasq.commons.daemon.lifecycle.Shutdown;

import my.package.ServiceDaemon;

public class ConsoleApplication
{
    public static void main(String[] args)
    {
        Daemon service = new ServiceDaemon();

        // perform initialization and start daemon
        service.init(null);
        service.start();

        // schedule shutdown hook for daemon cleanup
        Runtime.getRuntime().addShutdownHook(new Shutdown(service));
    }
}
```

# Custom finalization tasks

Why there is no possibility to bind custom finalization tasks or callbacks? `Shutdown` class handles only things that are normally handled by *Apache Commons Daemon* daemon handler. If you have any custom tasks that should be performed on your service shutdown, you need to implement them in your service thread - you need to make sure that they are always executed (for service daemon caller, like `jsvc` and for normal console application). The same way that adding custom logic to daemon handler won't affect console application, adding that logic to shutdown hook won't affect daemon runner flow.
