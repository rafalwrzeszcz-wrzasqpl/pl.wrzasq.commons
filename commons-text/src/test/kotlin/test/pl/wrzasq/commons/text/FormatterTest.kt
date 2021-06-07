/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.TextProcessingException
import pl.wrzasq.commons.text.Formatter
import pl.wrzasq.commons.text.formatter.HtmlFormatter

private const val FORMAT_HTML = "html"
private const val INPUT_TEXT = "<p>foo</p>"

class FormatterTest {
    @Test
    fun transform() {
        val formatter = Formatter()
        formatter.registerFormatter(FORMAT_HTML, HtmlFormatter())
        assertEquals(INPUT_TEXT, formatter.transform(FORMAT_HTML, INPUT_TEXT))
    }

    @Test
    fun transformUnknownException() {
        val formatter = Formatter()
        assertThrows(TextProcessingException::class.java) {
            formatter.transform(FORMAT_HTML, INPUT_TEXT)
        }
    }
}
