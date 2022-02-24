/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging.sns

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord
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
import pl.wrzasq.commons.aws.messaging.sns.TypedNotificationHandler
import test.pl.wrzasq.commons.aws.messaging.GenericMessage
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TypedNotificationHandlerTest {
    @MockK
    lateinit var messageHandler: (String) -> Unit

    @MockK
    lateinit var genericMessageHandler: (GenericMessage) -> Unit

    private val json = Json.Default

    @Test
    fun process() {
        val content = "test"

        val message = SNS().apply { message = json.encodeToString(content) }
        val record = SNSRecord().apply { setSns(message) }
        val event = SNSEvent().apply { records = listOf(record) }

        every { messageHandler(content) } just runs

        val handler = TypedNotificationHandler(json, messageHandler, json.serializersModule.serializer())
        handler.process(event)

        verify { messageHandler(content) }
    }

    @Test
    fun processGeneric() {
        val id0 = UUID.randomUUID()
        val id1 = UUID.randomUUID()
        val content = "{\"ids\":[\"${id0}\",\"${id1}\"]}"

        val message = SNS().apply { message = content }
        val record = SNSRecord().apply { setSns(message) }
        val event = SNSEvent().apply { records = listOf(record) }

        every { genericMessageHandler(any()) } just runs

        val handler = TypedNotificationHandler(json, genericMessageHandler, json.serializersModule.serializer())
        handler.process(event)

        val genericMessage = slot<GenericMessage>()

        verify { genericMessageHandler(capture(genericMessage)) }

        assertEquals(id0, genericMessage.captured.ids[0])
        assertEquals(id1, genericMessage.captured.ids[1])
    }
}
