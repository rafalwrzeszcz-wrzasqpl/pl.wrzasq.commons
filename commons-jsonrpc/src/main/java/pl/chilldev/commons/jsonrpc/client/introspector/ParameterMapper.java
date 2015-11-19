/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client.introspector;

import java.util.Map;

/**
 * Parameter type mapper.
 *
 * @param <Type> Mapping parameter type.
 */
@FunctionalInterface
public interface ParameterMapper<Type>
{
    /**
     * Populates call parameters with given value.
     *
     * @param name Parameter name.
     * @param value Parameter value passed to the call.
     * @param params Current state of RPC call parameters.
     */
    void putParam(String name, Type value, Map<String, Object> params);
}
