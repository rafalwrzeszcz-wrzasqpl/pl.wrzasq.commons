/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.mina;

// JUnit includes
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

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

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.mina.IoHandler;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

@RunWith(MockitoJUnitRunner.class)
public class IoHandlerTest
{
    @Mock
    private Dispatcher.RequestHandler<ContextInterface> versionHandler;

    @Mock
    private ContextInterface context;

    @Mock
    private IoHandlerAdapter handler;

    @Captor
    private ArgumentCaptor<Object> captor;

    @Test
    public void test_IoHandler_sessionIdle()
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        IoHandler<ContextInterface> handler = new IoHandler<>(null, null);

        // this is just to mark test coverage
        handler.sessionOpened(session);

        handler.sessionIdle(session, null);

        assertFalse("IoHandler.sessionIdle() should close connection for non-active session.", session.isConnected());
    }

    @Test
    public void test_IoHandler_exceptionCaught()
        throws
            Exception
    {
        IoHandler<ContextInterface> handler = new IoHandler<>(null, null);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.exceptionCaught(session, new Exception("test"));
        assertFalse("IoHandler.exceptionCaught() should close connection for session that caused exception.", session.isConnected());

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"Internal error\",\"code\":-32603},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void test_IoHandler_messageReceived()
        throws
            Exception
    {
        String id = "foo";
        String version = "bar";
        JSONRPC2Response response = new JSONRPC2Response(version, id);
        stub(this.versionHandler.process(isA(JSONRPC2Request.class), same(this.context))).toReturn(response);

        Dispatcher<ContextInterface> dispatcher = new Dispatcher<>();
        dispatcher.register("version", this.versionHandler);

        IoHandler<ContextInterface> handler = new IoHandler<>(this.context, dispatcher);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"version\"}");

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":\"" + id + "\",\"result\":\"" + version + "\",\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void test_IoHandler_messageReceived_JSONRPC2ParseException()
        throws
            Exception
    {
        IoHandler<ContextInterface> handler = new IoHandler<>(null, null);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.messageReceived(session, "invalid JSON");

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"JSON parse error\",\"code\":-32700},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }
}
