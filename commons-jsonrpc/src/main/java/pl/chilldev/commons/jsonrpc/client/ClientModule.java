/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;

/**
 * Client module SPI API.
 */
public interface ClientModule
{
    /**
     * Initializes client introspector.
     *
     * @param introspector Introspector.
     */
    void initializeIntrospector(Introspector introspector);
}
