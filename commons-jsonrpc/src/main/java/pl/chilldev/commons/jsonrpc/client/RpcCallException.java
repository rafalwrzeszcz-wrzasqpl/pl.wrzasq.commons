/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

/**
 * RPC exception method fail details.
 */
public class RpcCallException extends RuntimeException
{
    /**
     * Serializable class ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initializes error data.
     *
     * @param cause Root cause.
     */
    public RpcCallException(Exception cause)
    {
        super(cause.getMessage());
    }
}
