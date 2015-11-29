/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client;

import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.client.Connector;
import pl.chilldev.commons.jsonrpc.client.RpcCallException;
import pl.chilldev.commons.jsonrpc.mina.RequestIoHandler;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorTest
{
    private InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1234);

    private NioSocketConnector connector = new NioSocketConnector();

    @Mock
    private RequestIoHandler handler;

    @Mock
    private FutureTask<JSONRPC2Response> future;

    @Mock
    private ConnectFuture connectFuture;

    @Mock
    private IoSession session;

    @Test
    public void setMaxPacketSize()
    {
        Connector connector = Connector.create("127.0.0.1", 1234);

        int maxPacketSize = 123;
        connector.setMaxPacketSize(maxPacketSize);

        Assert.assertEquals(
            "Connector.setMaxPacketSize() should change maximum allowed packet size.",
            maxPacketSize,
            connector.getMaxPacketSize()
        );

        // just for code coverage
        connector.connect();
        connector.dispose();
    }

    @Test
    public void execute()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(true);

        connector.reconnect(this.connectFuture);

        String result = "OK";
        JSONRPC2Response response = new JSONRPC2Response(result, "id");

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenReturn(response);

        Assert.assertSame(
            "Connector.execute() should return executed method result.",
            result,
            connector.execute("test")
        );
    }

    @Test
    public void executeNewConnect()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);
        Connector spy = Mockito.spy(connector);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);

        String result = "OK";
        JSONRPC2Response response = new JSONRPC2Response(result, "id");

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenReturn(response);
        Mockito.doReturn(this.connectFuture).when(spy).connect();

        Assert.assertSame(
            "Connector.execute() should return executed method result.",
            result,
            spy.execute("test")
        );
    }

    @Test
    public void executeReconnect()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);
        Connector spy = Mockito.spy(connector);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(false);

        spy.reconnect(this.connectFuture);

        String result = "OK";
        JSONRPC2Response response = new JSONRPC2Response(result, "id");

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenReturn(response);
        Mockito.doReturn(this.connectFuture).when(spy).connect();

        Assert.assertSame(
            "Connector.execute() should return executed method result.",
            result,
            spy.execute("test")
        );
    }

    @Test
    public void executeWithParams()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(true);

        connector.reconnect(this.connectFuture);

        String result = "OK";
        JSONRPC2Response response = new JSONRPC2Response(result, "id");

        Map<String, Object> params = new HashMap<>();

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenReturn(response);

        Assert.assertSame(
            "Connector.execute() should return executed method result.",
            result,
            connector.execute("test", params)
        );
    }

    @Test(expected = RpcCallException.class)
    public void executeThrowsJSONRPC2Error()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(true);

        connector.reconnect(this.connectFuture);

        JSONRPC2Error error = new JSONRPC2Error(1, "error");
        JSONRPC2Response response = new JSONRPC2Response(error, "id");

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenReturn(response);

        connector.execute("test");
    }

    @Test(expected = RpcCallException.class)
    public void executeThrowsInterruptedException()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(true);

        connector.reconnect(this.connectFuture);

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenThrow(new InterruptedException());

        connector.execute("test");
    }

    @Test(expected = RpcCallException.class)
    public void executeThrowsExecutionException()
        throws
            InterruptedException,
            ExecutionException,
            JSONRPC2Error
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);
        Mockito.when(this.session.isConnected()).thenReturn(true);

        connector.reconnect(this.connectFuture);

        Mockito.when(this.handler.execute(Matchers.isA(JSONRPC2Request.class))).thenReturn(this.future);
        Mockito.when(this.future.get()).thenThrow(new ExecutionException(new Exception()));

        connector.execute("test");
    }

    @Test
    public void reconnect()
    {
        Connector connector = new Connector(this.connector, this.handler, this.address);

        Mockito.when(this.connectFuture.awaitUninterruptibly()).thenReturn(this.connectFuture);
        Mockito.when(this.connectFuture.getSession()).thenReturn(this.session);

        connector.reconnect(this.connectFuture);
    }
}
