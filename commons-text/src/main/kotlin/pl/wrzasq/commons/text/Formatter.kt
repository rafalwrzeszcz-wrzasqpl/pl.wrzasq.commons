/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text

import pl.wrzasq.commons.text.formatter.FormatterInterface

/**
 * Text formats handler.
 *
 * @param formatters All known format handlers.
 */
class Formatter(
    private val formatters: MutableMap<String, FormatterInterface> = mutableMapOf()
) {
    /**
     * Registers format handler.
     *
     * @param format Format name.
     * @param handler Format handler.
     */
    fun registerFormatter(format: String, handler: FormatterInterface) {
        formatters[format] = handler
    }

    /**
     * Transforms text from given form.
     *
     * @param format Format name.
     * @param text Source text.
     * @return Generated markup snippet.
     * @throws TextProcessingException When specified format can't be handled or handler throws exception.
     */
    fun transform(format: String, text: String): String = (
        formatters[format] ?: throw TextProcessingException("No handler for format \"${format}\" registered.")
        ).transform(text)
}
