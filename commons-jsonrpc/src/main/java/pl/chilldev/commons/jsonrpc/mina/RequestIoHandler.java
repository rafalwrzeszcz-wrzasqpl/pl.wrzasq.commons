/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

/**
 * JSON-RPC handler for TCP client session.
 */
public class RequestIoHandler extends IoHandlerAdapter
{
    /**
     * Request ID seed.
     */
    private static long id;

    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(RequestIoHandler.class);

    /**
     * Received responses.
     */
    protected Map<Object, FutureResponder<JSONRPC2Response>> responses = new HashMap<>();

    /**
     * Pending requests.
     */
    protected Queue<JSONRPC2Request> requests = new ConcurrentLinkedQueue<>();

    /**
     * Generates new request ID.
     *
     * @return Request ID.
     */
    protected static long generateRequestId()
    {
        return RequestIoHandler.id++;
    }

    /**
     * Queues request without parameters.
     *
     * @param method RPC method name.
     * @return Response future.
     */
    public FutureTask<JSONRPC2Response> execute(String method)
    {
        long id = RequestIoHandler.generateRequestId();

        this.requests.add(new JSONRPC2Request(method, id));

        return this.execute(id);
    }

    /**
     * Queues request without parameters.
     *
     * @param method RPC method name.
     * @param params Method params.
     * @return Response future.
     */
    public FutureTask<JSONRPC2Response> execute(String method, Map<String, Object> params)
    {
        long id = RequestIoHandler.generateRequestId();

        this.requests.add(new JSONRPC2Request(method, params, id));

        return this.execute(id);
    }

    /**
     * Generates response future.
     *
     * @param id Request ID.
     * @return Response future.
     */
    protected FutureTask<JSONRPC2Response> execute(long id)
    {
        FutureResponder<JSONRPC2Response> responder = new FutureResponder<>();
        FutureTask<JSONRPC2Response> future = new FutureTask<>(responder);
        responder.setFuture(future);
        this.responses.put(id, responder);
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

        JSONRPC2Request request;
        while ((request = this.requests.poll()) != null) {
            this.logger.debug("Session ID {}: JSON request: {}.", session.getId(), request);
            session.write(request);
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
    public void messageReceived(IoSession session, Object message)
        throws
            JSONRPC2ParseException
    {
        try {
            // parse the response
            JSONRPC2Response response = JSONRPC2Response.parse(message.toString());
            this.logger.debug("Session ID {}: JSON response: {}.", session.getId(), response);

            // dispatch it
            this.responses.get(response.getID()).setResponse(response);
            this.responses.remove(response.getID());
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
