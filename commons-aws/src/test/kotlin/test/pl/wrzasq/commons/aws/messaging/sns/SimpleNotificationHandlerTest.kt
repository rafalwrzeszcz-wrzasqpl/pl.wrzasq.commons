/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
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
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.sns.SimpleNotificationHandler

@ExtendWith(MockKExtension::class)
class SimpleNotificationHandlerTest {
    @MockK
    lateinit var messageHandler: (String) -> Unit

    @Test
    fun process() {
        every { messageHandler(any()) } just runs

        val message1 = SNS().apply { message = "msg1" }
        val record1 = SNSRecord().apply { setSns(message1) }

        val message2 = SNS().apply { message = "msg2" }
        val record2 = SNSRecord().apply { setSns(message2) }

        val event = SNSEvent().apply { records = listOf(record1, record2) }

        val handler = SimpleNotificationHandler(messageHandler)
        handler.process(event)

        verify { messageHandler(message1.message) }
        verify { messageHandler(message2.message) }
    }
}
