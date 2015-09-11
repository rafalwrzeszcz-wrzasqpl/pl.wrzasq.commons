/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc.handler;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import org.junit.Test;
import org.junit.Assert;

import pl.chilldev.commons.daemon.Package;

import pl.chilldev.commons.jsonrpc.rpc.handler.VersionHandler;

public class VersionHandlerTest
{
    @Test
    public void process()
    {
        VersionHandler handler = new VersionHandler();

        // create test request
        JSONRPC2Request request = new JSONRPC2Request("version", "test");
        Object result = handler.process(request, null);

        Assert.assertEquals(
            "VersionHandler.process() should return current service version.",
            Package.DEFAULT_PACKAGE.getVersion(),
            result.toString()
        );
    }

    @Test
    public void processPackage()
    {
        Package metadata = Package.DEFAULT_PACKAGE;
        VersionHandler handler = new VersionHandler(metadata);

        // create test request
        JSONRPC2Request request = new JSONRPC2Request("version", "test");
        Object result = handler.process(request, null);

        Assert.assertEquals(
            "VersionHandler.process() should return current service version.",
            metadata.getVersion(),
            result.toString()
        );
    }
}
