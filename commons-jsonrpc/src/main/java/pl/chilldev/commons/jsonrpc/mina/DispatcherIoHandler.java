/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.mina;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

/**
 * Single connection handler.
 *
 * @param <ContextType> Service request context type (will be used as context for request handlers).
 */
public class DispatcherIoHandler<ContextType extends ContextInterface> extends IoHandlerAdapter
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(DispatcherIoHandler.class);

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
    public DispatcherIoHandler(ContextType context, Dispatcher<? super ContextType> dispatcher)
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
    public void exceptionCaught(IoSession session, Throwable error)
    {
        this.logger.error(
            "Session ID {}: connection exteption.",
            session.getId(),
            error
        );
        session.write(new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, null));
        session.close(false);
    }

    /**
     * Handles new message.
     *
     * @param session Current connection session.
     * @param message Incomming message.
     */
    @Override
    public void messageReceived(IoSession session, Object message)
    {
        JSONRPC2Response response = null;

        try {
            // parse the request
            JSONRPC2Request request = JSONRPC2Request.parse(message.toString());
            this.logger.debug("Session ID {}: JSON request: {}.", session.getId(), request);

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

            this.logger.debug("JSON response: {}.", response);
        } catch (JSONRPC2ParseException error) {
            response = new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, null);
            this.logger.error("Could not parse JSON-RPC request.");
            this.logger.debug("Session ID {}: malformed JSON request: {}.", session.getId(), message);
        }

        // send response to client
        session.write(response);
    }

    /**
     * Handles idle connections.
     *
     * @param session Current connection session.
     * @param status Status.
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
    {
        this.logger.info("Session ID {}: closing idle connection.", session.getId());
        session.close(true);
    }

    /**
     * Handles new session logging.
     *
     * @param session Current connection session.
     */
    @Override
    public void sessionOpened(IoSession session)
    {
        this.logger.info("New connection from {}, connection ID: {}.", session.getRemoteAddress(), session.getId());
    }

    /**
     * Handles session closing.
     *
     * @param session Closed connection session.
     */
    @Override
    public void sessionClosed(IoSession session)
    {
        this.logger.debug("Connection from {} closed, connection ID: {}.", session.getRemoteAddress(), session.getId());
    }
}
