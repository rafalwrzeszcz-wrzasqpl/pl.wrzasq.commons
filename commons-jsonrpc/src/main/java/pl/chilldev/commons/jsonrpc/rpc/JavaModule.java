/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;

/**
 * Core Java types handling.
 */
public class JavaModule implements DispatcherModule
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeIntrospector(Introspector introspector)
    {
        // parameters retrievers

        // boolean retriever
        introspector.registerParameterProvider(
            boolean.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptBoolean(name, "true".equalsIgnoreCase(defaultValue))
                    : params.getBoolean(name)
        );

        // integer retriever
        introspector.registerParameterProvider(
            int.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptInt(name, Integer.parseInt(defaultValue))
                    : params.getInt(name)
        );

        // long retriever
        introspector.registerParameterProvider(
            long.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptLong(name, Long.parseLong(defaultValue))
                    : params.getLong(name)
        );

        // string retriever
        introspector.registerParameterProvider(
            String.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptString(name, defaultValue)
                    : params.getString(name)
        );

        // UUID retriever
        introspector.registerParameterProvider(
            UUID.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptUuid(name)
                    : params.getUuid(name)
        );

        // List retriever
        introspector.registerParameterProvider(
            List.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                optional
                    ? params.getOptList(name, Collections.singletonList(defaultValue))
                    : params.getList(name)
        );

        // Set retriever
        introspector.registerParameterProvider(
            Set.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) ->
                new HashSet<>(
                    optional
                        ? params.getOptList(name, Collections.singletonList(defaultValue))
                        : params.getList(name)
                )
        );
    }
}
