/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.netty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import io.netty.channel.ChannelHandlerContext;

import pl.chilldev.commons.jsonrpc.netty.RequestHandler;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerTest
{
    @Mock
    private ChannelHandlerContext session;

    @Test(expected = Exception.class)
    public void exceptionCaught()
        throws
            Exception
    {
        RequestHandler handler = new RequestHandler();

        handler.exceptionCaught(this.session, new Exception("test"));
    }

    @Test(expected = ExecutionException.class)
    public void channelInactive()
        throws
            Exception
    {
        RequestHandler handler = new RequestHandler();

        Future<JSONRPC2Response> future = handler.execute(new JSONRPC2Request("test", 1L));

        handler.channelInactive(this.session);

        future.get();
    }

    @Test
    public void channelRead()
        throws
            Exception
    {
        RequestHandler handler = new RequestHandler();

        Future<JSONRPC2Response> future = handler.execute(new JSONRPC2Request("test", 1L));

        handler.channelRead(this.session, "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"foo\"}");

        Assert.assertEquals(
            "RequestHandler.channelRead() should dispatch result to associated pending request future.",
            "foo",
            future.get().getResult()
        );
    }

    @Test
    public void channelReadUnknownId()
        throws
            JSONRPC2ParseException
    {
        RequestHandler handler = new RequestHandler();

        handler.channelRead(this.session, "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"foo\"}");
    }

    @Test(expected = JSONRPC2ParseException.class)
    public void channelReadJSONRPC2ParseException()
        throws
            JSONRPC2ParseException
    {
        RequestHandler handler = new RequestHandler();

        handler.channelRead(this.session, "invalid JSON");
    }
}
