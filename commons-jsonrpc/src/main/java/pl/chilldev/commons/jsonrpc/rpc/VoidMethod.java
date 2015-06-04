/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;

/**
 * Non-result RPC method wrapper.
 *
 * @param <ContextType> Service request context type (will be used as context for request handlers).
 */
@FunctionalInterface
public interface VoidMethod<ContextType extends ContextInterface>
{
    /**
     * JSON-RPC call handler.
     *
     * @param <ContextType> Service request context type.
     */
    class RequestHandler<ContextType extends ContextInterface>
        implements
            Dispatcher.RequestHandler<ContextType>
    {
        /**
         * RPC method handler.
         */
        protected VoidMethod<? super ContextType> method;

        /**
         * Initializes method wrapper.
         *
         * @param method RPC method.
         */
        public RequestHandler(VoidMethod<? super ContextType> method)
        {
            this.method = method;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JSONRPC2Response process(JSONRPC2Request request, ContextType context)
            throws
                JSONRPC2Error
        {
            this.method.process(request, context);
            return new JSONRPC2Response(request.getID());
        }
    }

    /**
     * Handles method call.
     *
     * @param request Request call.
     * @param context Execution context.
     * @throws JSONRPC2Error Error during JSON-RPC call handling.
     */
    void process(JSONRPC2Request request, ContextType context)
        throws
            JSONRPC2Error;
}
