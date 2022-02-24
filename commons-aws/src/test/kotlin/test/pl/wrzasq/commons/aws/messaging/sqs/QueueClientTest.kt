/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging.sqs

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.messaging.sqs.QueueClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
class QueueClientTest {
    @MockK
    lateinit var sqs: SqsClient

    @MockK
    lateinit var json: Json

    @Test
    fun send() {
        // just for code coverage
        QueueClient(queueUrl = "")

        val queue = "https://test"
        val input = Any()
        val message = "{}"
        val response = SendMessageResponse.builder().build()
        val client = QueueClient(json, sqs, queue)

        val captor = slot<Consumer<SendMessageRequest.Builder>>()

        every { json.encodeToString(input) } returns message
        every { sqs.sendMessage(capture(captor)) } returns response

        assertSame(response, client.send(input))

        val builder = SendMessageRequest.builder()
        captor.captured.accept(builder)
        val request = builder.build()

        assertEquals(message, request.messageBody())
        assertEquals(queue, request.queueUrl())
    }
}
