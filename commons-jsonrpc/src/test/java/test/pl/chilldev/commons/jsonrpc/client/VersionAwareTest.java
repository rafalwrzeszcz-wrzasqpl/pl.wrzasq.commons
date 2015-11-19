/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.jsonrpc.client.VersionAware;
import pl.chilldev.commons.jsonrpc.client.RpcCallException;

public class VersionAwareTest
{
    @Test
    public void version()
    {
        String version = "test";
        VersionAware client = () -> version;

        Assert.assertEquals(
            "VersionAware.version() should return version string.",
            version,
            client.getVersion()
        );
    }

    @Test
    public void versionException()
    {
        VersionAware client = () -> { throw new RpcCallException(new Exception()); };

        Assert.assertEquals(
            "VersionAware.version() should return offline version string when request fails.",
            VersionAware.OFFLINE_VERSION,
            client.getVersion()
        );
    }
}
