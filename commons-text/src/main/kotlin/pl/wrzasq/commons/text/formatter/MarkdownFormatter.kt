/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.formatter

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/**
 * Markdown format handler.
 *
 * @param flavour Markdown flavour handler.
 */
class MarkdownFormatter(
    private val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
) : FormatterInterface {
    override fun transform(text: String): String {
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(text)
        return HtmlGenerator(text, parsedTree, flavour).generateHtml().removeSurrounding("<body>", "</body>")
    }
}
