/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.slugifier

/**
 * Interface for URL slug generators.
 */
fun interface Slugifier {
    /**
     * Generates URL slug for given set of text.
     *
     * @param texts Plain text values.
     * @return URL-friendly slug.
     */
    fun slugify(vararg texts: String): String
}
