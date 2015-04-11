/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import java.util.HashMap;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;

/**
 * JSON-RPC request handling.
 *
 * @param <ContextType> Service request context type (will be used as context for request handlers).
 */
public class Dispatcher<ContextType extends ContextInterface>
{
    /**
     * JSON-RPC call.
     *
     * @param <ContextType> Service request context type.
     */
    public interface RequestHandler<ContextType extends ContextInterface>
    {
        /**
         * Handles request.
         *
         * @param request Request call.
         * @param context Execution context.
         * @return Call results.
         * @throws JSONRPC2Error Error during JSON-RPC call handling.
         */
        JSONRPC2Response process(JSONRPC2Request request, ContextType context)
            throws
                JSONRPC2Error;
    }

    /**
     * Registered RPC method handlers.
     */
    protected Map<String, RequestHandler<? super ContextType>> handlers = new HashMap<>();

    /**
     * Registers RPC handler.
     *
     * @param method RPC method.
     * @param handler Request handler for RPC method.
     */
    public void register(String method, RequestHandler<? super ContextType> handler)
    {
        this.handlers.put(method, handler);
    }

    /**
     * Registers result-returning method for RPC call.
     *
     * @param method RPC method.
     * @param rpcMethod RPC method.
     */
    public void register(String method, ReturningMethod<? super ContextType> rpcMethod)
    {
        this.register(method, new ReturningMethod.RequestHandler<ContextType>(rpcMethod));
    }

    /**
     * Registers no-result method for RPC call.
     *
     * @param method RPC method.
     * @param rpcMethod RPC method.
     */
    public void register(String method, VoidMethod<? super ContextType> rpcMethod)
    {
        this.register(method, new VoidMethod.RequestHandler<ContextType>(rpcMethod));
    }

    /**
     *  Handles request.
     *
     * @param request Request call.
     * @param context Execution context.
     * @return Call results.
     */
    public JSONRPC2Response dispatch(JSONRPC2Request request, ContextType context)
    {
        String method = request.getMethod();
        if (this.handlers.containsKey(method)) {
            try {
                return this.handlers.get(method).process(request, context);
            } catch (JSONRPC2Error error) {
                return new JSONRPC2Response(error, request.getID());
            }
        } else {
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
        }
    }
}
