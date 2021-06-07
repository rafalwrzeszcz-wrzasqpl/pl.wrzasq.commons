/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.TextProcessingException
import java.lang.Exception

class TextProcessingExceptionTest {
    @Test
    fun constructor() {
        val exception = TextProcessingException("foo")
        assertEquals("foo", exception.message)
    }

    @Test
    fun constructorWithCause() {
        val cause = Exception()
        val exception = TextProcessingException("foo", cause)
        assertEquals("foo", exception.message)
        assertSame(cause, exception.cause)
    }
}
