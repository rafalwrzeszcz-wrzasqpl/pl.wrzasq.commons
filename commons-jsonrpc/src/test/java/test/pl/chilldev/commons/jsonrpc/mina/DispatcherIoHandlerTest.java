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
import pl.chilldev.commons.jsonrpc.mina.DispatcherIoHandler;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherIoHandlerTest
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
    public void sessionIdle()
    {
        ProtocolCodecSession session = new ProtocolCodecSession();
        DispatcherIoHandler<ContextInterface> handler = new DispatcherIoHandler<>(null, null);

        // this is just to mark test coverage
        handler.sessionOpened(session);

        handler.sessionIdle(session, null);

        assertFalse("DispatcherIoHandler.sessionIdle() should close connection for non-active session.", session.isConnected());
    }

    @Test
    public void exceptionCaught()
        throws
            Exception
    {
        DispatcherIoHandler<ContextInterface> handler = new DispatcherIoHandler<>(null, null);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.exceptionCaught(session, new Exception("test"));
        assertFalse("DispatcherIoHandler.exceptionCaught() should close connection for session that caused exception.", session.isConnected());

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"Internal error\",\"code\":-32603},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void messageReceived()
        throws
            Exception
    {
        String id = "foo";
        String version = "bar";
        JSONRPC2Response response = new JSONRPC2Response(version, id);
        stub(this.versionHandler.process(isA(JSONRPC2Request.class), same(this.context))).toReturn(response);

        Dispatcher<ContextInterface> dispatcher = new Dispatcher<>();
        dispatcher.register("version", this.versionHandler);

        DispatcherIoHandler<ContextInterface> handler = new DispatcherIoHandler<>(this.context, dispatcher);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"version\"}");

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":\"" + id + "\",\"result\":\"" + version + "\",\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void messageReceivedThrowable()
        throws
            Exception
    {
        String id = "foo";
        String message = "bar";
        stub(this.versionHandler.process(isA(JSONRPC2Request.class), same(this.context))).toThrow(new RuntimeException(message));

        Dispatcher<ContextInterface> dispatcher = new Dispatcher<>();
        dispatcher.register("version", this.versionHandler);

        DispatcherIoHandler<ContextInterface> handler = new DispatcherIoHandler<>(this.context, dispatcher);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.messageReceived(session, "{\"jsonrpc\":\"2.0\",\"id\":\"" + id + "\",\"method\":\"version\"}");

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":\"" + id + "\",\"error\":{\"code\":-1,\"message\":\"Internal error: bar.\"},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void messageReceived_JSONRPC2ParseException()
        throws
            Exception
    {
        DispatcherIoHandler<ContextInterface> handler = new DispatcherIoHandler<>(null, null);

        ProtocolCodecSession session = new ProtocolCodecSession();
        session.setHandler(this.handler);

        handler.messageReceived(session, "invalid JSON");

        verify(this.handler).messageSent(same(session), this.captor.capture());
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"JSON parse error\",\"code\":-32700},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }
}
