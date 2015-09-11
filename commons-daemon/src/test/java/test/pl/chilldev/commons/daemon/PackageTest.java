/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.daemon.Package;

public class PackageTest
{
    @Test
    public void init()
    {
        Package meta = new Package();
        meta.init(Package.class.getResource(Package.DEFAULT_RESOURCE));

        Assert.assertEquals(
            "Package.init() should load version string from given properties stream.",
            "test",
            meta.getVersion()
        );
    }

    @Test
    public void initError()
        throws
            IOException
    {
        Package meta = new Package();
        meta.init(new URL("file:///unexisting/"));

        Assert.assertEquals(
            "Package.init() should set version string to \"error\" when properties loading fails on I/O.",
            "error",
            meta.getVersion()
        );
    }

    @Test
    public void initNull()
    {
        Package meta = new Package();
        meta.init(null);

        Assert.assertEquals(
            "Package.init() should set version string to \"devel\" if resource stream is unavailable.",
            "devel",
            meta.getVersion()
        );
    }

    @Test
    public void getVersion()
    {
        Package meta = new Package();
        meta.init();

        Assert.assertEquals(
            "Package.getVersion() should return loaded application version.",
            "test",
            meta.getVersion()
        );
    }

    @Test
    public void getVersionInit()
    {
        Package meta = new Package();

        Assert.assertEquals(
            "Package.getVersion() should automatically initialize metadata from default reosurce location.",
            "test",
            meta.getVersion()
        );
    }
}
