/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.filter.codec.ProtocolCodecSession;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import org.skyscreamer.jsonassert.JSONAssert;

import pl.chilldev.commons.jsonrpc.mina.RequestIoHandler;

@RunWith(MockitoJUnitRunner.class)
public class RequestIoHandlerTest
{
    @Mock
    private IoHandlerAdapter handler;

    @Captor
    private ArgumentCaptor<Object> captor;

    @Test(expected = TimeoutException.class)
    public void sessionIdle()
        throws
            TimeoutException
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

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
    public void execute()
        throws
            Exception
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        session.setHandler(this.handler);

        FutureTask<JSONRPC2Response> future = handler.execute("test");

        handler.sessionOpened(session);

        verify(this.handler).messageSent(same(session), this.captor.capture());

        String json = this.captor.getValue().toString();
        JSONRPC2Request request = JSONRPC2Request.parse(json);
        String id = request.getID().toString();
        JSONAssert.assertEquals("{\"id\":" + id + ",\"method\":\"test\",\"jsonrpc\":\"2.0\"}", json, true);

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"foo\"}");

        assertEquals(
            "RequestIoHandler.messageReceived() should dispatch result to associated pending request future.",
            "foo",
            future.get().getResult()
        );
    }

    @Test
    public void executeWithParams()
        throws
            Exception
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        RequestIoHandler handler = new RequestIoHandler();

        session.setHandler(this.handler);

        Map<String, Object> params = new HashMap<>();
        params.put("id", 123);
        FutureTask<JSONRPC2Response> future = handler.execute("test", params);

        handler.sessionOpened(session);

        verify(this.handler).messageSent(same(session), this.captor.capture());

        String json = this.captor.getValue().toString();
        JSONRPC2Request request = JSONRPC2Request.parse(json);
        String id = request.getID().toString();
        JSONAssert.assertEquals("{\"id\":" + id + ",\"method\":\"test\",\"params\":{\"id\":123},\"jsonrpc\":\"2.0\"}", json, true);

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"foo\"}");

        assertEquals(
            "RequestIoHandler.messageReceived() should dispatch result to associated pending request future.",
            "foo",
            future.get().getResult()
        );
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
