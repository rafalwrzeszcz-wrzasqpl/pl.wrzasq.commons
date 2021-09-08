/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.MessageHandler

@ExtendWith(MockKExtension::class)
class MessageHandlerTest {
    @MockK
    lateinit var messageHandler: (Int) -> Unit

    @Test
    fun handle() {
        every { messageHandler(any()) } just runs

        val objectMapper = ObjectMapper()
        val messageHandler = MessageHandler(
            objectMapper,
            messageHandler,
            Integer.TYPE
        )
        val message = 44
        messageHandler.handle(objectMapper.writeValueAsString(message))

        verify { messageHandler(message) }
    }

    @Test
    fun handleInvalidJson() {
        val objectMapper = ObjectMapper()
        val messageHandler = MessageHandler(
            objectMapper,
            messageHandler,
            Integer.TYPE
        )
        assertThrows<IllegalArgumentException> { messageHandler.handle("test") }
    }
}
