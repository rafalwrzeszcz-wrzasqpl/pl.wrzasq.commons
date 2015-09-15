/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.db.slugable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields that should represent slugs.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug
{
    /**
     * Fields to generate slug from.
     */
    String[] value();

    /**
     * Whether update slug or not.
     */
    boolean updatable() default true;

    /**
     * Prefix for the generated slugs.
     */
    String prefix() default "";

    /**
     * Suffix for the generated slugs.
     */
    String suffix() default "";
}
