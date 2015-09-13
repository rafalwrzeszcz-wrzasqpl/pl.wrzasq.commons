/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.daemon;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.daemon.Package;

import pl.chilldev.commons.jsonrpc.daemon.ChillDevApplication;

public class ChillDevApplicationTest
{
    public class TestApplication extends ChillDevApplication
    {
        @Override
        public String getDaemonName()
        {
            return super.getDaemonName();
        }

        @Override
        public String getDaemonVersion()
        {
            return super.getDaemonVersion();
        }
    }

    @Test
    public void getDaemonName()
    {
        ChillDevApplicationTest.TestApplication app = new ChillDevApplicationTest.TestApplication();

        Assert.assertEquals(
            "ChillDevApplication.getDaemonName() should return specified daemon name.",
            this.getClass().getPackage().getName(),
            app.getDaemonName()
        );
    }

    @Test
    public void getDaemonVersion()
    {
        ChillDevApplicationTest.TestApplication app = new ChillDevApplicationTest.TestApplication();

        Assert.assertEquals(
            "ChillDevApplication.getDaemonVersion() should return default package version.",
            Package.DEFAULT_PACKAGE.getVersion(),
            app.getDaemonVersion()
        );
    }
}
