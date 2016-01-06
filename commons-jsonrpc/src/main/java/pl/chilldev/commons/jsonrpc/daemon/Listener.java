/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.net.SocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.netty.DispatcherHandler;
import pl.chilldev.commons.jsonrpc.netty.StringChannelInitializer;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

/**
 * Single listener worker.
 *
 * @param <ContextType> Execution context type.
 */
public class Listener<ContextType extends ContextInterface>
    implements
        StringChannelInitializer.Configuration
{
    /**
     * Default packet size limit.
     */
    public static final int DEFAULT_PACKET_LIMIT = 33554432;

    /**
     * Netty logging handler.
     */
    public static final ChannelHandler LOGGING_HANDLER = new LoggingHandler(Listener.class);

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(Listener.class);

    /**
     * Listener name.
     */
    private String name;

    /**
     * API context.
     */
    private ContextType context;

    /**
     * JSON-RPC dispatcher.
     */
    private Dispatcher<? super ContextType> dispatcher;

    /**
     * Execution interval.
     */
    private Channel channel;

    /**
     * Listening address.
     */
    private SocketAddress address;

    /**
     * Maximum size of JSON-RPC packet.
     */
    private int maxPacketSize = Listener.DEFAULT_PACKET_LIMIT;

    /**
     * Initializes listener thread.
     *
     * @param name Thread title.
     * @param context API context.
     * @param dispatcher JSON-RPC dispatcher.
     */
    public Listener(String name, ContextType context, Dispatcher<? super ContextType> dispatcher)
    {
        this.name = name;
        this.context = context;
        this.dispatcher = dispatcher;
    }

    /**
     * Returns listener name.
     *
     * @return Listener name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets listening interface.
     *
     * @param address Listening address.
     */
    public void setAddress(SocketAddress address)
    {
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
     * Sets shutdown flag.
     *
     * @return Self instance.
     * @throws InterruptedException When the thread got interrupted when waiting for sockets to be closed.
     */
    public synchronized Listener<ContextType> stop()
        throws
            InterruptedException
    {
        if (this.channel != null) {
            this.channel.close().sync();
        }

        return this;
    }

    /**
     * Perform the thread loop.
     *
     * @param acceptors Acceptors thread pool.
     * @param workers Workers thread pool.
     * @throws InterruptedException When the thread got interrupted when waiting for sockets to be started.
     */
    public void start(EventLoopGroup acceptors, EventLoopGroup workers)
        throws
            InterruptedException
    {
        // check if there is any sense in running this listener
        if (this.address == null) {
            this.logger.info("\"{}\" was not enabled for connection, no point to start it.", this.getName());
            return;
        }

        // this is to make sure that possible .stop() calls will wait until server is started
        synchronized (this) {
            // network service configuration
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                .group(acceptors, workers)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(Listener.LOGGING_HANDLER)
                .childHandler(
                    new StringChannelInitializer<Channel>(
                        new DispatcherHandler<ContextType>(this.context, this.dispatcher),
                        this
                    )
                );

            // start the server
            this.channel = bootstrap.bind(this.address).sync().channel();
        }

        this.logger.info("Started.");
    }
}
