/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.slugifier

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.slugifier.SimpleSlugifier

private const val EXPECTED_SLUG = "chillout-development"

class SimpleSlugifierTest {
    @Test
    fun setDelimiter() {
        val slugifier = SimpleSlugifier("::")
        assertEquals("foo::bar", slugifier.slugify("foo", "bar"))
    }

    @Test
    fun slugify() {
        val slugifier = SimpleSlugifier()
        assertEquals("rafal", slugifier.slugify("rąfaĺ"))
    }

    @Test
    fun slugifyNonAscii() {
        val slugifier = SimpleSlugifier()
        assertEquals("chilloutdevelopment", slugifier.slugify("chillout»development"))
    }

    @Test
    fun slugifyNonLetters() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("chillout!development"))
    }

    @Test
    fun slugifySpaces() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("chillout - development"))
    }

    @Test
    fun slugifyLeading() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("---chillout - development"))
    }

    @Test
    fun slugifyTrailing() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("chillout - development---"))
    }

    @Test
    fun slugifyLowercase() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("ChillOut DevelopmenT"))
    }

    @Test
    fun slugifyMultiple() {
        val slugifier = SimpleSlugifier()
        assertEquals(EXPECTED_SLUG, slugifier.slugify("chillout", "development"))
    }
}
