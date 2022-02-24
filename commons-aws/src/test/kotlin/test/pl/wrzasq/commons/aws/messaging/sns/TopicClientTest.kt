/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.messaging.sns

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
import pl.wrzasq.commons.aws.messaging.sns.TopicClient
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
class TopicClientTest {
    @MockK
    lateinit var sns: SnsClient

    @MockK
    lateinit var json: Json

    @Test
    fun send() {
        // just for code coverage
        TopicClient(topicArn = "")

        val topic = "arn:test"
        val input = Any()
        val message = "{}"
        val response = PublishResponse.builder().build()
        val client = TopicClient(json, sns, topic)

        val captor = slot<Consumer<PublishRequest.Builder>>()

        every { json.encodeToString(input) } returns message
        every { sns.publish(capture(captor)) } returns response

        assertSame(response, client.send(input))

        val builder = PublishRequest.builder()
        captor.captured.accept(builder)
        val request = builder.build()

        assertEquals(message, request.message())
        assertEquals(topic, request.topicArn())
    }
}
