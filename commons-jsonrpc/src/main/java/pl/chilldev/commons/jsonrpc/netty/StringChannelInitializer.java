/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.netty;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import pl.chilldev.commons.jsonrpc.daemon.Listener;

/**
 * Utility class for Netty I/O channels.
 *
 * @param <ChannelType> Handled channel type.
 */
public class StringChannelInitializer<ChannelType extends Channel> extends ChannelInitializer<ChannelType>
{
    /**
     * Interface for I/O channel configuration.
     */
    public interface Configuration
    {
        /**
         * Returns maximum packet size.
         *
         * @return Maximum packet size.
         */
        int getMaxPacketSize();
    }

    /**
     * Class that serializes JSON-RPC message as string stream.
     */
    @ChannelHandler.Sharable
    private static class JsonRpc2MessageEncoder extends MessageToMessageEncoder<JSONRPC2Message>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void encode(ChannelHandlerContext context, JSONRPC2Message message, List<Object> out)
        {
            out.add(message.toJSONString() + "\n");
        }
    }

    /**
     * UTF-8 strings decoder.
     */
    private static final ChannelHandler STRING_DECODER = new StringDecoder(StandardCharsets.UTF_8);

    /**
     * UTF-8 strings encoder.
     */
    private static final ChannelHandler STRING_ENCODER = new StringEncoder(StandardCharsets.UTF_8);

    /**
     * JSON-RPC messages encoder.
     */
    private static final ChannelHandler JSONRPC_ENCODER = new StringChannelInitializer.JsonRpc2MessageEncoder();

    /**
     * Protocol handler for the channel.
     */
    private ChannelHandler handler;

    /**
     * Frame decoder.
     */
    private StringChannelInitializer.Configuration config;

    /**
     * Saves the initialization parameter for further channels.
     *
     * @param handler Protocol handler.
     * @param config Configuration.
     */
    public StringChannelInitializer(ChannelHandler handler, StringChannelInitializer.Configuration config)
    {
        this.handler = handler;
        this.config = config;
    }

    /**
     * Initializes I/O channel for text protocols.
     *
     * @param channel I/O connection channel.
     */
    @Override
    public void initChannel(ChannelType channel)
    {
        // network service configuration
        channel.pipeline().addLast(
            new LineBasedFrameDecoder(this.config.getMaxPacketSize()),
            StringChannelInitializer.STRING_DECODER,
            StringChannelInitializer.STRING_ENCODER,
            StringChannelInitializer.JSONRPC_ENCODER,
            Listener.LOGGING_HANDLER,
            this.handler
        );
    }
}
