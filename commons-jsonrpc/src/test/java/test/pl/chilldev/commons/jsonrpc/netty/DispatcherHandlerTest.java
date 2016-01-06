/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.netty;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import io.netty.channel.ChannelHandlerContext;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.skyscreamer.jsonassert.JSONAssert;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.netty.DispatcherHandler;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherHandlerTest
{
    @Mock
    private Dispatcher.RequestHandler<ContextInterface> versionHandler;

    @Mock
    private ContextInterface context;

    @Mock
    private ChannelHandlerContext session;

    @Captor
    private ArgumentCaptor<Object> captor;

    @Test
    public void exceptionCaught()
        throws
            Exception
    {
        DispatcherHandler<ContextInterface> handler = new DispatcherHandler<>(null, null);

        handler.exceptionCaught(this.session, new Exception("test"));

        Mockito.verify(this.session).writeAndFlush(this.captor.capture());
        Mockito.verify(this.session).close();
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"Internal error\",\"code\":-32603},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void channelRead()
        throws
            Exception
    {
        String id = "foo";
        String version = "bar";
        JSONRPC2Response response = new JSONRPC2Response(version, id);
        Mockito.stub(this.versionHandler.process(Matchers.isA(JSONRPC2Request.class), Matchers.same(this.context))).toReturn(response);

        Dispatcher<ContextInterface> dispatcher = new Dispatcher<>();
        dispatcher.register("version", this.versionHandler);

        DispatcherHandler<ContextInterface> handler = new DispatcherHandler<>(this.context, dispatcher);

        handler.channelRead(this.session, "{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"version\"}");

        Mockito.verify(this.session).writeAndFlush(this.captor.capture());
        JSONAssert.assertEquals("{\"id\":\"" + id + "\",\"result\":\"" + version + "\",\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void channelReadThrowable()
        throws
            Exception
    {
        String id = "foo";
        String message = "bar";
        Mockito.stub(this.versionHandler.process(Matchers.isA(JSONRPC2Request.class), Matchers.same(this.context))).toThrow(new RuntimeException(message));

        Dispatcher<ContextInterface> dispatcher = new Dispatcher<>();
        dispatcher.register("version", this.versionHandler);

        DispatcherHandler<ContextInterface> handler = new DispatcherHandler<>(this.context, dispatcher);

        handler.channelRead(this.session, "{\"jsonrpc\":\"2.0\",\"id\":\"" + id + "\",\"method\":\"version\"}");

        Mockito.verify(this.session).writeAndFlush(this.captor.capture());
        JSONAssert.assertEquals("{\"id\":\"" + id + "\",\"error\":{\"code\":-32603,\"message\":\"Internal error: bar.\"},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }

    @Test
    public void channelRead_JSONRPC2ParseException()
        throws
            Exception
    {
        DispatcherHandler<ContextInterface> handler = new DispatcherHandler<>(null, null);

        handler.channelRead(this.session, "invalid JSON");

        Mockito.verify(this.session).writeAndFlush(this.captor.capture());
        JSONAssert.assertEquals("{\"id\":null,\"error\":{\"message\":\"JSON parse error\",\"code\":-32700},\"jsonrpc\":\"2.0\"}", this.captor.getValue().toString(), true);
    }
}
