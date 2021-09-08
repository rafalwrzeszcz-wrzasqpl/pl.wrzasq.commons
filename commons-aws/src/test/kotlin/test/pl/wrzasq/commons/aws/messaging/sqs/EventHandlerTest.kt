/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging.sqs

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.sqs.EventHandler

@ExtendWith(MockKExtension::class)
class EventHandlerTest {
    @MockK
    lateinit var messageHandler: (SQSMessage) -> Unit

    @Test
    fun process() {
        every { messageHandler(any()) } just runs

        val message1 = SQSMessage().apply { body = "msg1" }
        val message2 = SQSMessage().apply { body = "msg2" }
        val event = SQSEvent().apply { records = listOf(message1, message2) }

        val handler = EventHandler(messageHandler)
        handler.process(event)

        verify { messageHandler(message1) }
        verify { messageHandler(message2) }
    }
}
