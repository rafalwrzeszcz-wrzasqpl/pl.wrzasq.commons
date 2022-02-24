/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.MessageDispatcher

@ExperimentalSerializationApi
@ExtendWith(MockKExtension::class)
class MessageDispatcherTest {
    @MockK
    lateinit var messageHandler: (String) -> String

    @MockK
    lateinit var serializerModule: SerializersModule

    @MockK
    lateinit var serializer: KSerializer<Any>

    @MockK
    lateinit var json: Json

    @Test
    fun handle() {
        val expected = "test"
        val client = MessageDispatcher(json, messageHandler)
        val message = 44
        val serialized = "44"

        every { json.serializersModule } returns serializerModule
        every { serializerModule.getContextual(Any::class, any()) } returns serializer
        every { json.encodeToString(serializer, message) } returns serialized
        every { messageHandler(serialized) } returns expected

        val response = client.send(message)

        verify { messageHandler(serialized) }

        assertEquals(expected, response)
    }

    @Test
    fun handleInvalidJson() {
        val client = MessageDispatcher(json, messageHandler)
        val message = 44

        every { json.serializersModule } returns serializerModule
        every { serializerModule.getContextual(Any::class, any()) } returns serializer
        every { json.encodeToString(serializer, message) } throws SerializationException()

        assertThrows<IllegalArgumentException> { client.send(message) }
    }
}
