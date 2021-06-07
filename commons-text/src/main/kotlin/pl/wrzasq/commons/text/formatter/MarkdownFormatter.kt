/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.formatter

import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

/**
 * Markdown format handler.
 *
 * @param markdownProcessor Markdown processor with all available extras.
 */
class MarkdownFormatter(
    private val markdownProcessor: PegDownProcessor = PegDownProcessor(Extensions.ALL)
) : FormatterInterface {
    override fun transform(text: String): String = markdownProcessor.markdownToHtml(text)
}
