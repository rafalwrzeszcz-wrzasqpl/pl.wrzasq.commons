/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;

/**
 * Server module SPI API.
 */
public interface DispatcherModule
{
    /**
     * Initializes server introspector.
     *
     * @param introspector Introspector.
     */
    void initializeIntrospector(Introspector introspector);
}
