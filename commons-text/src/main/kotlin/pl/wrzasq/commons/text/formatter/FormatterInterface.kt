/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.formatter

import pl.wrzasq.commons.text.TextProcessingException

/**
 * Source format handler interface.
 */
fun interface FormatterInterface {
    /**
     * Transforms source text into (X)HTML.
     *
     * @param text Source text.
     * @return (X)HTML snippet.
     * @throws TextProcessingException When text processing fails.
     */
    fun transform(text: String): String
}
