/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.slugifier

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.text.slugifier.Slugifier

class SlugifierTest {
    @Test
    fun slugify() {
        val slugifier = Slugifier { "foo" }
        assertEquals("foo", slugifier.slugify("test"))
    }
}
