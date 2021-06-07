/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.html

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.html.Utils.firstParagraph
import pl.wrzasq.commons.text.html.Utils.truncate
import pl.wrzasq.commons.text.html.Utils.setFormatter
import pl.wrzasq.commons.text.html.Utils.format
import pl.wrzasq.commons.text.html.Utils.urlEncode
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.text.formatter.FormatterInterface
import kotlin.Throws
import pl.wrzasq.commons.text.TextProcessingException
import pl.wrzasq.commons.text.Formatter

private const val HELLO_WORLD = "Hello world!"

@ExtendWith(MockKExtension::class)
class UtilsTest {
    @MockK
    lateinit var formatHandler: FormatterInterface

    @Test
    fun firstParagraph() {
        val value = "foo <span>bar</span>\n<em>baz</em>"
        val html = "<p>$value</p> <p>\nquux</p>"
        assertEquals(value, firstParagraph(html))
    }

    @Test
    fun firstParagraphEmpty() {
        val value = "foo <span>bar</span>\n<em>baz</em>"
        assertEquals("", firstParagraph(value))
    }

    @Test
    fun truncateShortText() {
        val text = "Hello"
        assertEquals(text, truncate(text, 7))
    }

    @Test
    fun truncate() {
        assertEquals("Hello…", truncate(HELLO_WORLD, 7))
    }

    @Test
    fun truncateWordBounds() {
        assertEquals("Hello w…", truncate(HELLO_WORLD, 7, false))
    }

    @Test
    fun truncateWordBoundsNoWord() {
        val text = "Helloworld!"
        assertEquals("Hellowo…", truncate(text, 7, true))
    }

    @Test
    fun truncateSuffix() {
        assertEquals("Hello.", truncate(HELLO_WORLD, 7, "."))
    }

    @Test
    fun truncateSuffixWordBounds() {
        assertEquals("Hello w.", truncate(HELLO_WORLD, 7, ".", false))
    }

    @Test
    @Throws(TextProcessingException::class)
    fun format() {
        val formatter = Formatter()
        formatter.registerFormatter("foo", formatHandler)
        val input = "bar"
        val result = "baz"

        every {
            formatHandler.transform(input)
        } returns result

        setFormatter(formatter)
        assertEquals(result, format("foo", input))

        verify {
            formatHandler.transform(input)
        }
    }

    @Test
    fun urlEncode() {
        assertEquals("foo+bar%2F%E2%80%A6", urlEncode("foo bar/…"))
    }
}
