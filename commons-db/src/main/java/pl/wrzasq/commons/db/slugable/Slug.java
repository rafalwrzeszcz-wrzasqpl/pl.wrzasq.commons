/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.db.slugable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields that should represent slugs.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug {
    /**
     * Fields to generate slug from.
     *
     * @return Fields names.
     */
    String[] value();

    /**
     * Whether update slug or not.
     *
     * @return Update flag.
     */
    boolean updatable() default true;

    /**
     * Prefix for the generated slugs.
     *
     * @return Prefix.
     */
    String prefix() default "";

    /**
     * Suffix for the generated slugs.
     *
     * @return Suffix.
     */
    String suffix() default "";
}
