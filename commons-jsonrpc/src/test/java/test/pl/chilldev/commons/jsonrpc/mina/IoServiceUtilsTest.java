/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.mina;

import org.junit.Assert;
import org.junit.Test;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import pl.chilldev.commons.jsonrpc.mina.IoServiceUtils;

public class IoServiceUtilsTest
{
    @Test
    public void initialize()
    {
        // just to increase code coverage
        new IoServiceUtils();

        final int size = 1024;

        // simple configuration
        IoServiceUtils.Configuration configuration = new IoServiceUtils.Configuration() {
            @Override
            public int getMaxPacketSize()
            {
                return size;
            }
        };

        IoHandlerAdapter handler = new IoHandlerAdapter();
        NioSocketAcceptor service = new NioSocketAcceptor();
        IoServiceUtils.initialize(service, handler, configuration);

        Assert.assertSame("IoServiceUtils.initialize() should set request handler on given socket.", handler, service.getHandler());
    }
}
