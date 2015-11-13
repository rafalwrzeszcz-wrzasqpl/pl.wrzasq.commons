/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc.introspector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows for defining JSON-RPC parameter metadata.
 *
 * <p>
 * Important note - by default RPC parameters are mandatory, but default value of <tt>options()</tt> flag in this
 * annotation is <tt>true</tt>, which means that using this annotation changes default status of the parameter.
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcParam
{
    /**
     * RPC parameter name (if not specified, name from Java method signature will be used).
     *
     * @return Parameter name.
     */
    String name() default "";

    /**
     * Sets default value (note that setting `defaultValue` makes `optional` meaningless).
     *
     * @return Default parameter value.
     */
    String defaultValue() default "";

    /**
     * Sets default value of the parameter to NULL.
     *
     * @return Default value NULL flag.
     */
    boolean defaultNull() default false;

    /**
     * Marks parameter as optional.
     *
     * @return Optional flag (true by default).
     */
    boolean optional() default true;
}
