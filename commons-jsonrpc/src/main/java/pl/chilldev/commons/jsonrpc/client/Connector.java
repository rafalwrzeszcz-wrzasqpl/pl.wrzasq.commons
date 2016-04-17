/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.netty.RequestHandler;
import pl.chilldev.commons.jsonrpc.netty.StringChannelInitializer;

/**
 * Single TCP client.
 */
public class Connector
    implements
        StringChannelInitializer.Configuration
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
    private Logger logger = LoggerFactory.getLogger(Connector.class);

    /**
     * Destination address.
     */
    private SocketAddress address;

    /**
     * Maximum size of JSON-RPC packet.
     */
    @Getter
    @Setter
    private int maxPacketSize = Connector.DEFAULT_PACKET_LIMIT;

    /**
     * Client connector bootstrap.
     */
    private Bootstrap bootstrap = new Bootstrap();

    /**
     * JSON-RCP handler.
     */
    private RequestHandler handler;

    /**
     * Current connection session.
     */
    private Channel session;

    /**
     * Initializes connector with given configuration.
     *
     * @param connectors Connectors thread pool.
     * @param handler JSON-RPC request handler.
     * @param address Server address.
     */
    public Connector(EventLoopGroup connectors, RequestHandler handler, SocketAddress address)
    {
        this.handler = handler;
        this.address = address;

        this.bootstrap
            .group(connectors)
            .channel(NioSocketChannel.class)
            .handler(new StringChannelInitializer<Channel>(this.handler, this));
    }

    /**
     * Executes remote call without parameters.
     *
     * @param method Method name.
     * @return Response.
     * @throws RpcCallException When execution fails.
     */
    public Object execute(String method)
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
    private Object execute(JSONRPC2Request request)
    {
        // (re-)connect if needed
        if (this.session == null || !this.session.isActive()) {
            this.reconnect(this.connect());
        }

        try {
            // first register request to listen for the response
            Future<JSONRPC2Response> future = this.handler.execute(request);

            // send request to server
            this.session.writeAndFlush(request);

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
            throw new RpcCallException(error.getCause());
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
    public ChannelFuture connect()
    {
        return this.bootstrap.connect(this.address);
    }

    /**
     * Enforces connection to server.
     *
     * @param future Connection establishing future.
     */
    public void reconnect(ChannelFuture future)
    {
        this.session = future.syncUninterruptibly().channel();
    }

    /**
     * Creates connector instance with default resources.
     *
     * @param connectors Connectors thread pool.
     * @param address Server address.
     * @return Client connector.
     */
    public static Connector create(EventLoopGroup connectors, SocketAddress address)
    {
        return new Connector(connectors, new RequestHandler(), address);
    }

    /**
     * Creates connector instance with default resources.
     *
     * @param connectors Connectors thread pool.
     * @param host Listen host.
     * @param port Listen port.
     * @return Client connector.
     */
    public static Connector create(EventLoopGroup connectors, String host, int port)
    {
        return Connector.create(connectors, new InetSocketAddress(host, port));
    }

    /**
     * Generates new request ID.
     *
     * @return Request ID.
     */
    private static long generateRequestId()
    {
        return Connector.id++;
    }
}
