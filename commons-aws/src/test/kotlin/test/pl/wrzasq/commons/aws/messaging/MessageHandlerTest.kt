/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.MessageHandler

@ExtendWith(MockKExtension::class)
class MessageHandlerTest {
    @MockK
    lateinit var messageHandler: (Int) -> Unit

    private val json = Json.Default

    @Test
    fun handle() {
        every { messageHandler(any()) } just runs

        val messageHandler = MessageHandler(
            json,
            messageHandler,
            json.serializersModule.serializer()
        )
        val message = 44
        messageHandler.handle(json.encodeToString(message))

        verify { messageHandler(message) }
    }

    @Test
    fun handleInvalidJson() {
        val messageHandler = MessageHandler(
            json,
            messageHandler,
            json.serializersModule.serializer()
        )
        assertThrows<IllegalArgumentException> { messageHandler.handle("test") }
    }
}
