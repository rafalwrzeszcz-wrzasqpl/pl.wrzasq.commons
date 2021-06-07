/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 - 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.formatter

import org.apache.commons.text.StringEscapeUtils

/**
 * Plain text format handler.
 */
class PlainTextFormatter : FormatterInterface {
    override fun transform(text: String): String = StringEscapeUtils.ESCAPE_XML10.translate(text)
        .replace("\n".toRegex(), "<br/>")
}
