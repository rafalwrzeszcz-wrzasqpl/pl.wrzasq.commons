/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.formatter

/**
 * (X)HTML format handler.
 */
class HtmlFormatter : FormatterInterface {
    // this is already our desired format
    override fun transform(text: String): String = text
}
