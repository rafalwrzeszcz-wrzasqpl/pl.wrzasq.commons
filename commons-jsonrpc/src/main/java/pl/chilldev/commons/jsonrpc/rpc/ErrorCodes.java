/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

/**
 * Generic JSON-RPC error codes.
 */
public class ErrorCodes
{
    /**
     * General unknown exception unhandled by an application.
     */
    public static final int CODE_INTERNAL = -1;

    /**
     * Internal error stub.
     */
    public static final JSONRPC2Error ERROR_INTERNAL = new JSONRPC2Error(
        ErrorCodes.CODE_INTERNAL,
        "Internal error"
    );
}
