/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc.introspector;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;

/**
 * Parameter provider.
 *
 * @param <Type> Resolving parameter type.
 */
@FunctionalInterface
public interface ParameterProvider<Type>
{
    /**
     * Fetches parameter from given context.
     *
     * @param name Parameter name.
     * @param params Request parameters.
     * @param optional Optional flag.
     * @param defaultValue Default parameter value.
     * @return Resolved parameter.
     * @throws JSONRPC2Error When resolving parameter fails.
     */
    Type getParam(String name, ParamsRetriever params, boolean optional, String defaultValue)
        throws
            JSONRPC2Error;
}
