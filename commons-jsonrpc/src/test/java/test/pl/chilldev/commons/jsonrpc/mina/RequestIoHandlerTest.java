/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.mina;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.filter.codec.ProtocolCodecSession;

import pl.chilldev.commons.jsonrpc.mina.RequestIoHandler;
import pl.chilldev.commons.jsonrpc.rpc.ErrorCodes;

public class RequestIoHandlerTest
{
    @Test(expected = TimeoutException.class)
    public void sessionIdle()
        throws
            TimeoutException
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        // just for code coverage
        handler.sessionOpened(session);
        handler.messageSent(session, null);

        handler.sessionIdle(session, null);
    }

    @Test(expected = Exception.class)
    public void exceptionCaught()
        throws
            Exception
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        handler.exceptionCaught(session, new Exception("test"));
    }

    @Test
    public void sessionClosed()
        throws
            Exception
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        FutureTask<JSONRPC2Response> future = handler.execute(new JSONRPC2Request("test", 1L));

        handler.sessionClosed(session);

        Assert.assertFalse(
            "RequestIoHandler.sessionClosed() should terminate all pending requests.",
            future.get().indicatesSuccess()
        );
        Assert.assertEquals(
            "RequestIoHandler.sessionClosed() should terminate all pending requests.",
            ErrorCodes.CODE_CONNECTION,
            future.get().getError().getCode()
        );
    }

    @Test
    public void messageReceived()
        throws
            Exception
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        FutureTask<JSONRPC2Response> future = handler.execute(new JSONRPC2Request("test", 1L));

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"foo\"}");

        Assert.assertEquals(
            "RequestIoHandler.messageReceived() should dispatch result to associated pending request future.",
            "foo",
            future.get().getResult()
        );
    }

    @Test
    public void messageReceivedUnknownId()
        throws
            JSONRPC2ParseException
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"foo\"}");
    }

    @Test(expected = JSONRPC2ParseException.class)
    public void messageReceivedJSONRPC2ParseException()
        throws
            JSONRPC2ParseException
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        handler.messageReceived(session, "invalid JSON");
    }
}
