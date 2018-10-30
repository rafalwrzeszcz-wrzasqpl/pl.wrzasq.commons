/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.daemon.Package;

public class PackageTest
{
    @Test
    public void init()
    {
        Package meta = new Package();
        meta.init(Package.class.getResource(Package.DEFAULT_RESOURCE));

        Assertions.assertEquals(
            "test",
            meta.getVersion(),
            "Package.init() should load version string from given properties stream."
        );
    }

    @Test
    public void initOpenError()
        throws
            IOException
    {
        Package meta = new Package();
        meta.init(new URL("file:///unexisting/"));

        Assertions.assertEquals(
            "error",
            meta.getVersion(),
            "Package.init() should set version string to \"error\" when properties loading fails on I/O."
        );
    }

    @Test
    public void initReadError()
        throws
            IOException
    {
        Package meta = new Package()
        {
            @Override
            public void init(InputStream stream)
                throws
                    IOException
            {
                throw new IOException();
            }
        };
        meta.init(Package.class.getResource(Package.DEFAULT_RESOURCE));

        Assertions.assertEquals(
            "error",
            meta.getVersion(),
            "Package.init() should set version string to \"error\" when properties loading fails on I/O."
        );
    }

    @Test
    public void initNull()
    {
        Package meta = new Package();
        meta.init(null);

        Assertions.assertEquals(
            "devel",
            meta.getVersion(),
            "Package.init() should set version string to \"devel\" if resource stream is unavailable."
        );
    }

    @Test
    public void getVersion()
    {
        Package meta = new Package();
        meta.init();

        Assertions.assertEquals(
            "test",
            meta.getVersion(),
            "Package.getVersion() should return loaded application version."
        );
    }

    @Test
    public void getVersionInit()
    {
        Package meta = new Package();

        Assertions.assertEquals(
            "test",
            meta.getVersion(),
            "Package.getVersion() should automatically initialize metadata from default resource location."
        );
    }
}
