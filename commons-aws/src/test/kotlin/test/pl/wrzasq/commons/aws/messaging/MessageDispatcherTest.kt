/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.MessageDispatcher

@ExtendWith(MockKExtension::class)
class MessageDispatcherTest {
    @MockK
    lateinit var messageHandler: (String) -> String

    @MockK
    lateinit var objectMapper: ObjectMapper

    @Test
    fun handle() {
        val expected = "test"
        val client = MessageDispatcher(objectMapper, messageHandler)
        val message = 44
        val serialized = "44"

        every { objectMapper.writeValueAsString(message) } returns serialized
        every { messageHandler(serialized) } returns expected

        val response = client.send(message)

        verify { messageHandler(serialized) }

        assertEquals(expected, response)
    }

    @Test
    fun handleInvalidJson() {
        val client = MessageDispatcher(objectMapper, messageHandler)
        val message = 44

        every { objectMapper.writeValueAsString(message) } throws JsonParseException(null, "")
        assertThrows<IllegalArgumentException> { client.send(message) }
    }
}
