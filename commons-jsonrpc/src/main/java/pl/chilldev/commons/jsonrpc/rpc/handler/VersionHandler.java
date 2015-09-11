/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc.handler;

// dependencies and sub-modules
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import pl.chilldev.commons.daemon.Package;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.rpc.ReturningMethod;

/**
 * General JSON-RPC version() call.
 *
 * <p>
 * Request params:
 *  <tt>none</tt>
 * </p>
 *
 * <p>
 * Response:
 *  <tt>string</tt> (version)
 * </p>
 */
public class VersionHandler
    implements
        ReturningMethod<ContextInterface>
{
    /**
     * Application package data.
     */
    protected Package metadata;

    /**
     * Default version initialization.
     */
    public VersionHandler()
    {
        this(Package.DEFAULT_PACKAGE);
    }

    /**
     * Initializes version handler with given package metadata.
     *
     * @param metadata Package metadata.
     */
    public VersionHandler(Package metadata)
    {
        this.metadata = metadata;
    }

    /**
     * Handles request.
     *
     * @param request Request call.
     * @param context Execution context.
     * @return Call results.
     */
    @Override
    public Object process(JSONRPC2Request request, ContextInterface context)
    {
        return this.metadata.getVersion();
    }
}
