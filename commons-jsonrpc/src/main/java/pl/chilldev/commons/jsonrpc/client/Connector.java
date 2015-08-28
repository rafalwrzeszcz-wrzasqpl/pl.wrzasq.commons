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
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
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
     * Request ID seed.
     */
    private static long id;

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
     * Current connection session.
     */
    protected IoSession session;

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

        IoServiceUtils.initialize(this.connector, this.handler, this);
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
     * @throws RpcCallException When execution fails.
     */
    public Object execute(String method)
        throws
            RpcCallException
    {
        long id = Connector.generateRequestId();

        return this.execute(new JSONRPC2Request(method, id));
    }

    /**
     * Executes remote call with parameters.
     *
     * @param method Method name.
     * @param params RPC params.
     * @return Response.
     * @throws RpcCallException When execution fails.
     */
    public Object execute(String method, Map<String, Object> params)
        throws
            RpcCallException
    {
        long id = Connector.generateRequestId();

        return this.execute(new JSONRPC2Request(method, params, id));
    }

    /**
     * Executes queued RPC call.
     *
     * @param request JSON-RPC request.
     * @return Method result.
     * @throws RpcCallException When execution fails.
     */
    protected Object execute(JSONRPC2Request request)
        throws
            RpcCallException
    {
        // (re-)connect if needed
        if (this.session == null || !this.session.isConnected()) {
            this.reconnect(this.connect());
        }

        try {
            // first register request to listen for the response
            FutureTask<JSONRPC2Response> future = this.handler.execute(request);

            // send request to server
            this.session.write(request);

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
            throw new RpcCallException(error);
        } catch (ExecutionException error) {
            this.logger.error("Error while executing asynchronous response handler: {}.", error.getMessage());
            throw new RpcCallException(error);
        } catch (JSONRPC2Error error) {
            this.logger.error("Server returned error response: {}.", error.getMessage());
            throw new RpcCallException(error);
        }
    }

    /**
     * Connects to server.
     *
     * @return Connection establishing future.
     */
    public ConnectFuture connect()
    {
        return this.connector.connect(this.address);
    }

    /**
     * Enforces connection to server.
     *
     * @param future Connection establishing future.
     */
    public void reconnect(ConnectFuture future)
    {
        this.session = future.awaitUninterruptibly().getSession();
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

    /**
     * Generates new request ID.
     *
     * @return Request ID.
     */
    protected static long generateRequestId()
    {
        return Connector.id++;
    }
}
