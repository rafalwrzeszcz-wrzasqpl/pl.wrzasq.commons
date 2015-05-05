/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.mina.IoServiceUtils;
import pl.chilldev.commons.jsonrpc.mina.RequestIoHandler;

/**
 * Single TCP client.
 */
public class Connector
    implements
        IoServiceUtils.Configuration
{
    /**
     * Default packet size limit.
     */
    public static final int DEFAULT_PACKET_LIMIT = 33554432;

    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(Connector.class);

    /**
     * Listening address.
     */
    protected InetSocketAddress address;

    /**
     * Maximum size of JSON-RPC packet.
     */
    protected int maxPacketSize = Connector.DEFAULT_PACKET_LIMIT;

    /**
     * Connector itself.
     */
    protected NioSocketConnector connector;

    /**
     * JSON-RCP handler.
     */
    protected RequestIoHandler handler;

    /**
     * Initializes connector with given configuration.
     *
     * @param connector Socket connector.
     * @param handler JSON-RPC request handler.
     * @param address Listening address.
     */
    public Connector(NioSocketConnector connector, RequestIoHandler handler, InetSocketAddress address)
    {
        this.connector = connector;
        this.handler = handler;
        this.address = address;
    }

    /**
     * Sets max JSON-RPC packet size.
     *
     * @param maxPacketSize Packet size in bytes.
     */
    public void setMaxPacketSize(int maxPacketSize)
    {
        this.maxPacketSize = maxPacketSize;
    }

    /**
     * Returns size of maximum JSON-RPC packet size.
     *
     * @return Maximum packet size.
     */
    @Override
    public int getMaxPacketSize()
    {
        return this.maxPacketSize;
    }

    /**
     * Executes remote call without parameters.
     *
     * @param method Method name.
     * @return Response.
     * @throws ExecutionException When execution fails on client side or due to a connection.
     * @throws JSONRPC2Error When execution fails on server side.
     */
    public Object execute(String method)
        throws
            ExecutionException,
            JSONRPC2Error
    {
        return this.execute(this.handler.execute(method));
    }

    /**
     * Executes remote call with parameters.
     *
     * @param method Method name.
     * @param params RPC params.
     * @return Response.
     * @throws ExecutionException When execution fails on client side or due to a connection.
     * @throws JSONRPC2Error When execution fails on server side.
     */
    public Object execute(String method, Map<String, Object> params)
        throws
            ExecutionException,
            JSONRPC2Error
    {
        return this.execute(this.handler.execute(method, params));
    }

    /**
     * Executes queued RPC call.
     *
     * @param future Response fetcher future.
     * @return Method result.
     * @throws ExecutionException When execution fails on client side or due to a connection.
     * @throws JSONRPC2Error When execution fails on server side.
     */
    protected Object execute(FutureTask<JSONRPC2Response> future)
        throws
            ExecutionException,
            JSONRPC2Error
    {
        try {
            JSONRPC2Response response = future.get();

            // server-side error
            if (!response.indicatesSuccess()) {
                JSONRPC2Error error = response.getError();

                this.logger.error(
                    "Response ID: {}, error #{}: {}.",
                    response.getID(),
                    error.getCode(),
                    error.getMessage()
                );
                throw error;
            }

            return response.getResult();
        } catch (InterruptedException error) {
            this.logger.error("Error while waiting for asynchronous response: {}.", error.getMessage());
            throw new ExecutionException(error);
        } catch (ExecutionException error) {
            this.logger.error("Error while executing asynchronous response handler: {}.", error.getMessage());
            throw error;
        }
    }

    /**
     * Connects to server.
     *
     * @return Connection establishing future.
     */
    public ConnectFuture connect()
    {
        IoServiceUtils.initialize(this.connector, this.handler, this);
        return this.connector.connect(this.address);
    }

    /**
     * Releases all resources.
     */
    public void dispose()
    {
        this.connector.dispose();
    }

    /**
     * Creates connector instance with default resources.
     *
     * @param address Server address.
     * @return Client connector.
     */
    public static Connector create(InetSocketAddress address)
    {
        return new Connector(new NioSocketConnector(), new RequestIoHandler(), address);
    }
}
