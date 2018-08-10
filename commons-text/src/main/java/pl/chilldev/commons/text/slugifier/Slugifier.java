/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.slugifier;

/**
 * Interface for URL slug generators.
 */
@FunctionalInterface
public interface Slugifier
{
    /**
     * Generates URL slug for given text.
     *
     * @param text Plain text value.
     * @return URL-friendly slug.
     */
    default String slugify(String text)
    {
        return this.slugify(new String[] {text});
    }

    /**
     * Generates URL slug for given set of text.
     *
     * @param texts Plain text values.
     * @return URL-friendly slug.
     */
    String slugify(String... texts);
}
