/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import org.junit.Test;

import pl.chilldev.commons.jsonrpc.daemon.AbstractSpringApplication;

public class AbstractSpringApplicationTest
{
    public class SpringApplication extends AbstractSpringApplication
    {
        @Override
        protected String getDaemonName()
        {
            return "test.pl.chilldev.commons.jsonrpc.daemon";
        }

        @Override
        protected String getDaemonVersion()
        {
            return "0.0.0";
        }
    }

    // it's just for code coverage and regression checks right now, no real assertions

    @Test
    public void start()
    {
        AbstractSpringApplicationTest.SpringApplication app = new AbstractSpringApplicationTest.SpringApplication();

        app.start();
    }

    @Test
    public void stop()
    {
        AbstractSpringApplicationTest.SpringApplication app = new AbstractSpringApplicationTest.SpringApplication();

        app.start();
        app.stop();
    }

    @Test
    public void stopWithoutContext()
    {
        AbstractSpringApplicationTest.SpringApplication app = new AbstractSpringApplicationTest.SpringApplication();

        app.stop();
    }
}
