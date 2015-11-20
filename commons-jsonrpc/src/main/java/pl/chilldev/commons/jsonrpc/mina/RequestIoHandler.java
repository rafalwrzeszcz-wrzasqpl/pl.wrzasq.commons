/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.concurrent.FutureResponder;

import pl.chilldev.commons.jsonrpc.rpc.ErrorCodes;

/**
 * JSON-RPC handler for TCP client session.
 */
public class RequestIoHandler extends IoHandlerAdapter
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(RequestIoHandler.class);

    /**
     * Received responses.
     */
    private Map<Object, FutureResponder<JSONRPC2Response>> responses = new HashMap<>();

    /**
     * Generates response future.
     *
     * @param request JSON-RPC request.
     * @return Response future.
     */
    public FutureTask<JSONRPC2Response> execute(JSONRPC2Request request)
    {
        // prepares response handling future
        FutureResponder<JSONRPC2Response> responder = new FutureResponder<>();
        FutureTask<JSONRPC2Response> future = new FutureTask<>(responder);
        responder.setFuture(future);
        this.responses.put(request.getID(), responder);

        return future;
    }

    /**
     * Handles new session logging.
     *
     * @param session Current connection session.
     */
    @Override
    public void sessionOpened(IoSession session)
    {
        this.logger.info("New connection to {}, connection ID: {}.", session.getRemoteAddress(), session.getId());
    }

    /**
     * Handles session closing.
     *
     * @param session Closed connection session.
     */
    @Override
    public void sessionClosed(IoSession session)
    {
        this.logger.debug("Connection to {} closed, connection ID: {}.", session.getRemoteAddress(), session.getId());

        // fail all pending requests
        synchronized (this.responses) {
            for (Map.Entry<Object, FutureResponder<JSONRPC2Response>> entry : this.responses.entrySet()) {
                this.logger.error("Terminating request ID {}: lost connection.", entry.getKey());
                entry.getValue().setResponse(
                    new JSONRPC2Response(
                        ErrorCodes.ERROR_CONNECTION.appendMessage(": lost connection."),
                        entry.getKey()
                    )
                );
            }
            this.responses.clear();
        }
    }

    /**
     * Handles outgoing message.
     *
     * @param session Current connection session.
     * @param message Incomming message.
     */
    @Override
    public void messageSent(IoSession session, Object message)
    {
        this.logger.debug("Session ID {}: JSON request: {}.", session.getId(), message);
    }

    /**
     * Handles new message.
     *
     * @param session Current connection session.
     * @param message Incomming message.
     * @throws JSONRPC2ParseException Error occured when parsing response.
     */
    @Override
    public void messageReceived(IoSession session, Object message)
        throws
            JSONRPC2ParseException
    {
        try {
            // parse the response
            JSONRPC2Response response = JSONRPC2Response.parse(message.toString());
            this.logger.debug("Session ID {}: JSON response: {}.", session.getId(), response);

            Object id = response.getID();
            if (this.responses.containsKey(id)) {
                // dispatch it
                this.responses.get(id).setResponse(response);
                this.responses.remove(id);
            } else {
                this.logger.warn("Session ID {}: response for unknown request ID {}.", session.getId(), id);
            }
        } catch (JSONRPC2ParseException error) {
            this.logger.error("Could not parse JSON-RPC response.");
            this.logger.debug("Session ID {}: malformed JSON response: {}.", session.getId(), message);
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
    public void exceptionCaught(IoSession session, Throwable error)
        throws
            Exception
    {
        this.logger.error(
            "Session ID {}: connection exteption.",
            session.getId(),
            error
        );
        session.close(false);
        throw new Exception(error);
    }

    /**
     * Handles idle connections.
     *
     * @param session Current connection session.
     * @param status Status.
     * @throws TimeoutException Connection timeout.
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
        throws
            TimeoutException
    {
        this.logger.warn("Session ID {}: closing idle connection.", session.getId());
        session.close(true);
        throw new TimeoutException(String.format("Connection timeout out: session ID %s.", session.getId()));
    }
}
