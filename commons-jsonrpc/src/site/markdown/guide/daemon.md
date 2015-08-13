<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Daemon application

Once you have a [listener](./listener.html) implemented you can use it in your application. But for simple RPC services listener, dispatcher and request handlers is all that you need. It would require a lot of bloat-code to expose the run just a listener thread and manage it. [**Apache Commons Daemon**](http://commons.apache.org/proper/commons-daemon/) is a simple library that allows you to write simple daemon applications in **Java**. What's more, `pl.chilldev.commons:commons-jsonrpc` package comes with partially-implemented abstract classes that even simplifies the task by assuming that all you need to do is to manage listener threads.

Daemon application, by default (you can simply override any of the daemon method) behavies like this:

-   starting the daemon starts all the listener threads;
-   stopping the daemon stops all the listener threads (interrupts if needed);
-   `SIGUSR2` handling will stop and then start again all listener threads (not much gain, but entire **JVM** bootstrapping goes away).

### Maven dependenies

In order to use base implementation of daemon application you will have to add additional dependency to your project (to save space and time for people who don't want to use JSON-RPC package without full application this package is marked as optional in our library `pom.xml`):

```xml
<dependency>
    <groupId>commons-daemon</groupId>
    <artifactId>commons-daemon</artifactId>
    <version>${apache.commons.daemon.version}</version>
</dependency>
```

### Implementing

All you have to implement in your concrete class is to implement two simple informational method and the core method that builds listeners. Additionally it may also be a good idea to implement `main()` method to allow direct running through CLI interface:

```java
import pl.chilldev.commons.jsonrpc.daemon.AbstractApplication;

/**
 * Application daemon manager.
 */
public class App extends AbstractApplication
{
    /**
     * Application entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        App app = new App();

        try {
            // perform initialization and start daemon
            app.init(null);
            app.start();

            // schedule shutdown hook for daemon cleanup
            Runtime.getRuntime().addShutdownHook(new Shutdown(app));
        } catch (Throwable error) {
            app.logger.error("Fatal error {}.", error.getMessage(), error);
            System.exit(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonName()
    {
        return "example-daemon";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonVersion()
    {
        return "0.0.1";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Listener> buildListeners()
    {
        // build list of listeners and return it - don't start them!
    }
}
```

## Spring integration

To make things even easier, if you want to use **Spring** in your application you can also use it to manage listener threads.

### Maven dependencies

In order to use Spring-based application class you need to add additional two dependencies (together with previous ones) to your project:

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
    <version>${spring.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>${spring.version}</version>
</dependency>
```

### Implementing

When using Spring IoC container, you are expected to not set up listener threads objects on your own, but let Spring do that. Instead of implementing `buildListeners()` method you need to register `protected String getPackageToScan()` method that specifies base package to scan for container configuration:

```java
import pl.chilldev.commons.jsonrpc.daemon.AbstractSpringApplication;

/**
 * Application daemon manager.
 */
public class App extends AbstractSpringApplication
{
    /**
     * Application entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        App app = new App();

        try {
            // perform initialization and start daemon
            app.init(null);
            app.start();

            // schedule shutdown hook for daemon cleanup
            Runtime.getRuntime().addShutdownHook(new Shutdown(app));
        } catch (Throwable error) {
            app.logger.error("Fatal error {}.", error.getMessage(), error);
            System.exit(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonName()
    {
        return "your-daemon";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonVersion()
    {
        return "0.0.1";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPackageToScan()
    {
        return "com.package.your";
    }
}
```
