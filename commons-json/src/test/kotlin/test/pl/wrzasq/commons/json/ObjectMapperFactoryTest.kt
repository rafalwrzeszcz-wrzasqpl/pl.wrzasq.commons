/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.json

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.json.ObjectMapperFactory.createObjectMapper
import pl.wrzasq.commons.json.ObjectMapperFactory
import java.time.LocalDate
import java.time.Month

class ObjectMapperFactoryTest {
    @Test
    fun handleJava8TimeApiSerialization() {
        val objectMapper = createObjectMapper()
        val date = LocalDate.of(2011, Month.JANUARY, 30)
        val json = objectMapper.writeValueAsString(date)
        assertEquals("\"2011-01-30\"", json)
    }

    @Test
    fun handleJava8TimeApiDeserialization() {
        val objectMapper = createObjectMapper()
        val date = objectMapper.readValue("\"2015-07-02\"", LocalDate::class.java)
        assertEquals(2015, date.year)
        assertEquals(Month.JULY, date.month)
        assertEquals(2, date.dayOfMonth)
    }

    @Test
    fun handleUnknownProperties() {
        val objectMapper = createObjectMapper()
        objectMapper.readValue("{\"nonExisting\":12}", ObjectMapperFactory::class.java)

        // if there is no exception everything is fine
    }
}
