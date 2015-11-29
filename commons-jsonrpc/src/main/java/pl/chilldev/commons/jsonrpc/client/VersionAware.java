/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;

/**
 * Mixin for version-aware service client.
 */
public interface VersionAware
{
    /**
     * Version placeholder to use when no server is available.
     */
    String OFFLINE_VERSION = "OFFLINE";

    /**
     * Executed version request.
     *
     * @return Version returned by server.
     * @throws RpcCallException When remote call fails.
     */
    @JsonRpcCall
    String version();

    /**
     * Returns service version.
     *
     * @return Version string.
     */
    default String getVersion()
    {
        try {
            return this.version();
        } catch (RpcCallException error) {
            return VersionAware.OFFLINE_VERSION;
        }
    }
}
