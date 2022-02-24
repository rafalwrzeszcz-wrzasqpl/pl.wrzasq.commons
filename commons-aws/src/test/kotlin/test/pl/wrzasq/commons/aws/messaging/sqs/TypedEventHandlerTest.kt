/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019, 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging.sqs

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.sqs.TypedEventHandler
import test.pl.wrzasq.commons.aws.messaging.GenericMessage
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TypedEventHandlerTest {
    @MockK
    lateinit var messageHandler: (String) -> Unit

    @MockK
    lateinit var genericMessageHandler: (GenericMessage) -> Unit

    private val json = Json.Default

    @Test
    fun process() {
        val content = "test"

        val message = SQSMessage().apply { body = json.encodeToString(content) }
        val event = SQSEvent().apply { records = listOf(message) }

        every { messageHandler(content) } just runs

        val handler = TypedEventHandler(json, messageHandler, json.serializersModule.serializer())
        handler.process(event)

        verify { messageHandler(content) }
    }

    @Test
    fun processGeneric() {
        val id0 = UUID.randomUUID()
        val id1 = UUID.randomUUID()
        val content = "{\"ids\":[\"${id0}\",\"${id1}\"]}"

        val message = SQSMessage().apply { body = content }
        val event = SQSEvent().apply { records = listOf(message) }

        every { genericMessageHandler(any()) } just runs

        val handler = TypedEventHandler(json, genericMessageHandler, json.serializersModule.serializer())
        handler.process(event)

        val genericMessage = slot<GenericMessage>()

        verify { genericMessageHandler(capture(genericMessage)) }

        assertEquals(id0, genericMessage.captured.ids[0])
        assertEquals(id1, genericMessage.captured.ids[1])
    }
}
