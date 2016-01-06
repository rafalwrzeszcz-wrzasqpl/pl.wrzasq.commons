/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.netty;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

/**
 * Single connection handler.
 *
 * @param <ContextType> Service request context type (will be used as context for request handlers).
 */
@ChannelHandler.Sharable
public class DispatcherHandler<ContextType extends ContextInterface> extends ChannelInboundHandlerAdapter
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    /**
     * Execution context.
     */
    private ContextType context;

    /**
     * JSON-RPC dispatcher.
     */
    private Dispatcher<? super ContextType> dispatcher;

    /**
     * Initializes JSON-RPC binding.
     *
     * @param context Execution context.
     * @param dispatcher JSON-RPC dispatcher.
     */
    public DispatcherHandler(ContextType context, Dispatcher<? super ContextType> dispatcher)
    {
        super();
        this.context = context;
        this.dispatcher = dispatcher;
    }

    /**
     * Handles thrown error.
     *
     * @param session Current connection session.
     * @param error Error.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable error)
    {
        this.logger.error(
            "Session ID {}: connection exteption.",
            session.name(),
            error
        );
        session.writeAndFlush(new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, null));
        session.close();
    }

    /**
     * Handles new message.
     *
     * @param session Current connection session.
     * @param message Incomming message.
     */
    @Override
    public void channelRead(ChannelHandlerContext session, Object message)
    {
        JSONRPC2Response response = null;

        try {
            // parse the request
            JSONRPC2Request request = JSONRPC2Request.parse(message.toString());

            // dispatch it
            try {
                response = this.dispatcher.dispatch(request, this.context);
                //CHECKSTYLE:OFF: IllegalCatchCheck
            } catch (Throwable error) {
                //CHECKSTYLE:ON: IllegalCatchCheck
                // we DO WANT to catch all exceptions to avoid listener thread to die
                this.logger.error("Internal error.", error);
                response = new JSONRPC2Response(
                    JSONRPC2Error.INTERNAL_ERROR.appendMessage(": " + error.getMessage() + "."),
                    request.getID()
                );
            }
        } catch (JSONRPC2ParseException error) {
            response = new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, null);
            this.logger.error("Could not parse JSON-RPC request.");
        }

        // send response to client
        session.writeAndFlush(response);
    }
}
