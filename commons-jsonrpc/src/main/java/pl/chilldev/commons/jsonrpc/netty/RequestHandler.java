/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.rpc.ErrorCodes;

/**
 * JSON-RPC handler for TCP client session.
 */
@ChannelHandler.Sharable
public class RequestHandler extends ChannelInboundHandlerAdapter
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * Received responses.
     */
    private Map<Object, Promise<JSONRPC2Response>> responses = new HashMap<>();

    /**
     * Generates response future.
     *
     * @param request JSON-RPC request.
     * @return Response future.
     */
    public Future<JSONRPC2Response> execute(JSONRPC2Request request)
    {
        // prepares response handling future
        Promise<JSONRPC2Response> responder = GlobalEventExecutor.INSTANCE.newPromise();
        this.responses.put(request.getID(), responder);

        return responder;
    }

    /**
     * Handles session closing.
     *
     * @param session Closed connection session.
     */
    @Override
    public void channelInactive(ChannelHandlerContext session)
    {
        // fail all pending requests
        synchronized (this.responses) {
            for (Map.Entry<Object, Promise<JSONRPC2Response>> entry : this.responses.entrySet()) {
                this.logger.error("Terminating request ID {}: lost connection.", entry.getKey());
                entry.getValue().setFailure(
                    ErrorCodes.ERROR_CONNECTION.appendMessage(": lost connection.")
                );
            }
            this.responses.clear();
        }
    }

    /**
     * Handles new message.
     *
     * @param session Current connection session.
     * @param message Incomming message.
     * @throws JSONRPC2ParseException Error occured when parsing response.
     */
    @Override
    public void channelRead(ChannelHandlerContext session, Object message)
        throws
            JSONRPC2ParseException
    {
        try {
            // parse the response
            JSONRPC2Response response = JSONRPC2Response.parse(message.toString());

            Object id = response.getID();
            if (this.responses.containsKey(id)) {
                // dispatch it
                this.responses.get(id).setSuccess(response);
                this.responses.remove(id);
            } else {
                this.logger.warn("Session ID {}: response for unknown request ID {}.", session.name(), id);
            }
        } catch (JSONRPC2ParseException error) {
            this.logger.error("Could not parse JSON-RPC response.");
            throw error;
        }
    }

    /**
     * Handles thrown error.
     *
     * @param session Current connection session.
     * @param error Error.
     * @throws Exception Exception that triggered the event.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable error)
        throws
            Exception
    {
        this.logger.error(
            "Session ID {}: connection exteption.",
            session.name(),
            error
        );
        session.close();
        throw new Exception(error);
    }
}
