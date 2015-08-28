/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client;

import org.junit.Test;

import pl.chilldev.commons.jsonrpc.client.RpcCallException;

public class RpcCallExceptionTest
{
    @Test
    public void constructor()
    {
        // just for the sake of code coverate
        new RpcCallException(new Exception("error"));
    }
}
