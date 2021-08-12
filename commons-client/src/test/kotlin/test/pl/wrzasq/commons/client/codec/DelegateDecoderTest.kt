/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.codec

import java.nio.charset.StandardCharsets
import feign.Request
import feign.Response
import feign.codec.Decoder
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.client.codec.DelegateDecoder
import pl.wrzasq.commons.client.codec.HEADER_NAME_CONTENT_TYPE

private const val MEDIA_TYPE_APPLICATION_JSON = "application/json"
private const val MEDIA_TYPE_TEXT_PLAIN = "text/plain"

@ExtendWith(MockKExtension::class)
class DelegateDecoderTest {
    @MockK
    lateinit var fallback: Decoder

    @MockK
    lateinit var typed: Decoder

    private val request = Request.create(
        Request.HttpMethod.HEAD,
        "/",
        emptyMap(),
        byteArrayOf(),
        StandardCharsets.UTF_8,
        null
    )

    @Test
    fun decode() {
        val response = createResponse(request)
            .headers(
                mapOf(
                    HEADER_NAME_CONTENT_TYPE to setOf(MEDIA_TYPE_APPLICATION_JSON)
                )
            )
            .build()
        val result = this

        every {
            typed.decode(response, String::class.java)
        } returns result

        val decoder = DelegateDecoder(fallback)
        decoder.registerTypeDecoder(MEDIA_TYPE_APPLICATION_JSON, typed)
        assertSame(result, decoder.decode(response, String::class.java))

        verify { fallback wasNot called }
        verify { typed.decode(response, String::class.java) }
    }

    @Test
    fun decodeWithExtraParameters() {
        val response = createResponse(request)
            .headers(
                mapOf(
                    HEADER_NAME_CONTENT_TYPE to setOf("$MEDIA_TYPE_APPLICATION_JSON;charset=UTF-8")
                )
            )
            .build()
        val result = this

        every {
            typed.decode(response, String::class.java)
        } returns result

        val decoder = DelegateDecoder(fallback)
        decoder.registerTypeDecoder(MEDIA_TYPE_APPLICATION_JSON, typed)
        assertSame(result, decoder.decode(response, String::class.java))

        verify { fallback wasNot called }
        verify { typed.decode(response, String::class.java) }
    }

    @Test
    fun decodeWithoutType() {
        val response = createResponse(request)
            .headers(emptyMap())
            .build()
        val result = this

        every {
            fallback.decode(response, String::class.java)
        } returns result

        val decoder = DelegateDecoder(fallback)
        decoder.registerTypeDecoder(MEDIA_TYPE_APPLICATION_JSON, typed)
        assertSame(result, decoder.decode(response, String::class.java))

        verify { fallback.decode(response, String::class.java) }
        verify { typed wasNot called }
    }

    @Test
    fun decodeFallback() {
        val response = createResponse(request)
            .headers(
                mapOf(
                    HEADER_NAME_CONTENT_TYPE to setOf(MEDIA_TYPE_TEXT_PLAIN)
                )
            )
            .build()
        val result = this

        every {
            fallback.decode(response, String::class.java)
        } returns result

        val decoder = DelegateDecoder(fallback)
        decoder.registerTypeDecoder(MEDIA_TYPE_APPLICATION_JSON, typed)
        assertSame(result, decoder.decode(response, String::class.java))

        verify { fallback.decode(response, String::class.java) }
        verify { typed wasNot called }
    }

    private fun createResponse(request: Request): Response.Builder = Response.builder()
        .status(200)
        .reason("ok")
        .request(request)
}
