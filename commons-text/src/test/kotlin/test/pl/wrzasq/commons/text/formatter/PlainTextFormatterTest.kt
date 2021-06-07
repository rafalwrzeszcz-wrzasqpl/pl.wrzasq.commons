/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.formatter.PlainTextFormatter

class PlainTextFormatterTest {
    @Test
    fun transform() {
        val textFormatter = PlainTextFormatter()
        assertEquals("&lt;span&gt;foo<br/>&lt;/span&gt;", textFormatter.transform("<span>foo\n</span>"))
    }
}
