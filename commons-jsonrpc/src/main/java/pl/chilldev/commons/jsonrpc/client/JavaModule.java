/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import java.util.UUID;

import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;

/**
 * Core Java types handling.
 */
public class JavaModule implements ClientModule
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeIntrospector(Introspector introspector)
    {
        // response types handlers

        // UUID handling
        introspector.registerResultHandler(
            UUID.class,
            (Object response) -> UUID.fromString(response.toString())
        );
    }
}
