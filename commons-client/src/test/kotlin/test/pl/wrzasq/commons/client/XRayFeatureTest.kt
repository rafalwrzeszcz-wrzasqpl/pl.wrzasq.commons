/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorder
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import com.amazonaws.xray.entities.TraceHeader
import com.amazonaws.xray.entities.TraceID
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.client.XRayFeature

private const val HOST = "wrzasq.pl"
private const val PATH = "/blog"
private const val KEY_RESPONSE = "response"
private const val KEY_STATUS = "status"
private const val KEY_CONTENT_LENGTH = "content_length"

@ExtendWith(MockKExtension::class)
class XRayFeatureTest {
    @MockK
    lateinit var recorder: AWSXRayRecorder

    @MockK
    lateinit var scope: HttpClient

    @MockK
    lateinit var subsegment: Subsegment

    @MockK
    lateinit var traceId: TraceID

    @MockK
    lateinit var parentSegment: Segment

    @MockK
    lateinit var httpRequestBuilder: HttpRequestBuilder

    @MockK
    lateinit var httpResponse: HttpResponse

    @MockK
    lateinit var httpClientCall: HttpClientCall

    @MockK
    lateinit var requestContext: PipelineContext<Any, HttpRequestBuilder>

    @MockK
    lateinit var responseContext: PipelineContext<HttpResponse, HttpClientCall>

    @Test
    fun prepare() {
        XRayFeature.prepare {
            assertSame(AWSXRay.getGlobalRecorder(), recorder)
        }
    }

    @Test
    fun requestNotPropagate() {
        val feature = XRayFeature(recorder)
        val request = installFeature(feature).first

        prepareRequestTest()

        every { subsegment.shouldPropagate() } returns false

        runBlocking { request(requestContext, this) }

        val slot = slot<Map<String, String>>()

        verify { httpRequestBuilder.attributes.put(any(), subsegment) }
        verify { subsegment.setNamespace("remote") }
        verify(exactly = 0) { httpRequestBuilder.headers.append(TraceHeader.HEADER_KEY, any()) }
        verify { subsegment.putHttp("request", capture(slot)) }
        assertEquals("https://wrzasq.pl/blog", slot.captured["url"])
        assertEquals(HttpMethod.Put.value, slot.captured["method"])
    }

    @Test
    fun requestPropagateNotSampled() {
        val feature = XRayFeature(recorder)
        val request = installFeature(feature).first

        prepareRequestTest()

        every { subsegment.shouldPropagate() } returns true
        every { subsegment.parentSegment } returns parentSegment
        every { parentSegment.traceId } returns traceId
        every { parentSegment.isSampled } returns false
        every { httpRequestBuilder.headers.append(any(), any()) } just runs

        runBlocking { request(requestContext, this) }

        verify(exactly = 0) { subsegment.id }
        verify { httpRequestBuilder.headers.append(TraceHeader.HEADER_KEY, any()) }
    }

    @Test
    fun requestPropagateSampled() {
        val feature = XRayFeature(recorder)
        val request = installFeature(feature).first

        prepareRequestTest()

        every { subsegment.shouldPropagate() } returns true
        every { subsegment.parentSegment } returns parentSegment
        every { parentSegment.traceId } returns traceId
        every { parentSegment.isSampled } returns true
        every { subsegment.id } returns "test"
        every { httpRequestBuilder.headers.append(any(), any()) } just runs

        runBlocking { request(requestContext, this) }

        verify { subsegment.id }
        verify { httpRequestBuilder.headers.append(TraceHeader.HEADER_KEY, any()) }
    }

    @Test
    fun response2xx() {
        val feature = XRayFeature(recorder)
        val response = installFeature(feature).second

        prepareResponseTest()

        every { httpResponse.status } returns HttpStatusCode.OK
        every { httpResponse.headers[HttpHeaders.ContentLength] } returns "100"

        runBlocking { response(responseContext, httpResponse) }

        val slot = slot<Map<String, Any>>()

        verify { recorder.endSubsegment(subsegment) }
        verify { subsegment.putHttp(KEY_RESPONSE, capture(slot)) }

        assertEquals(HttpStatusCode.OK.value, slot.captured[KEY_STATUS])
        assertEquals(100L, slot.captured[KEY_CONTENT_LENGTH])
    }

    @Test
    fun response4xx() {
        val feature = XRayFeature(recorder)
        val response = installFeature(feature).second

        prepareResponseTest()

        every { httpResponse.status } returns HttpStatusCode.NotFound
        every { httpResponse.headers[HttpHeaders.ContentLength] } returns null

        runBlocking { response(responseContext, httpResponse) }

        val slot = slot<Map<String, Any>>()

        verify { recorder.endSubsegment(subsegment) }
        verify { subsegment.putHttp(KEY_RESPONSE, capture(slot)) }
        verify { subsegment.isError = true }
        verify(exactly = 0) { subsegment.isFault = any() }
        verify(exactly = 0) { subsegment.isThrottle = any() }

        assertEquals(HttpStatusCode.NotFound.value, slot.captured[KEY_STATUS])
        assertFalse(slot.captured.containsKey(KEY_CONTENT_LENGTH))
    }

    @Test
    fun response429() {
        val feature = XRayFeature(recorder)
        val response = installFeature(feature).second

        prepareResponseTest()

        every { httpResponse.status } returns HttpStatusCode.TooManyRequests
        every { httpResponse.headers[HttpHeaders.ContentLength] } returns null

        runBlocking { response(responseContext, httpResponse) }

        val slot = slot<Map<String, Any>>()

        verify { recorder.endSubsegment(subsegment) }
        verify { subsegment.putHttp(KEY_RESPONSE, capture(slot)) }
        verify { subsegment.isError = true }
        verify(exactly = 0) { subsegment.isFault = any() }
        verify { subsegment.isThrottle = true }

        assertEquals(HttpStatusCode.TooManyRequests.value, slot.captured[KEY_STATUS])
        assertFalse(slot.captured.containsKey(KEY_CONTENT_LENGTH))
    }

    @Test
    fun response500() {
        val feature = XRayFeature(recorder)
        val response = installFeature(feature).second

        prepareResponseTest()

        every { httpResponse.status } returns HttpStatusCode.InternalServerError
        every { httpResponse.headers["Content-Length"] } returns null

        runBlocking { response(responseContext, httpResponse) }

        val slot = slot<Map<String, Any>>()

        verify { recorder.endSubsegment(subsegment) }
        verify { subsegment.putHttp(KEY_RESPONSE, capture(slot)) }
        verify(exactly = 0) { subsegment.isError = any() }
        verify { subsegment.isFault = true }
        verify(exactly = 0) { subsegment.isThrottle = any() }

        assertEquals(HttpStatusCode.InternalServerError.value, slot.captured[KEY_STATUS])
        assertFalse(slot.captured.containsKey(KEY_CONTENT_LENGTH))
    }

    private fun installFeature(feature: XRayFeature): Pair<
        PipelineInterceptor<Any, HttpRequestBuilder>,
        PipelineInterceptor<HttpResponse, HttpClientCall>
    > {
        val request = slot<PipelineInterceptor<Any, HttpRequestBuilder>>()
        val response = slot<PipelineInterceptor<HttpResponse, HttpClientCall>>()
        every { scope.sendPipeline.intercept(HttpSendPipeline.Monitoring, capture(request)) } just runs
        every { scope.receivePipeline.intercept(HttpReceivePipeline.State, capture(response)) } just runs
        XRayFeature.install(feature, scope)

        return request.captured to response.captured
    }

    private fun prepareRequestTest() {
        every { recorder.beginSubsegment(HOST) } returns subsegment
        every { httpRequestBuilder.method } returns HttpMethod.Put
        every { httpRequestBuilder.url } returns URLBuilder(
            host = HOST,
            protocol = URLProtocol.HTTPS,
            encodedPath = PATH
        )
        every { httpRequestBuilder.attributes.put(any(), subsegment) } just runs
        every { subsegment.setNamespace(any()) } just runs
        every { subsegment.putHttp(any(), any()) } just runs
        every { requestContext.context } returns httpRequestBuilder
        coEvery { requestContext.proceed() } returns this
    }

    private fun prepareResponseTest() {
        every { recorder.endSubsegment(subsegment) } just runs
        every { subsegment.isError = any() } just runs
        every { subsegment.isFault = any() } just runs
        every { subsegment.isThrottle = any() } just runs
        every { subsegment.putHttp("response", any()) } just runs
        every { responseContext.context } returns httpClientCall
        every { httpClientCall.response } returns httpResponse
        every { httpClientCall.attributes[any<AttributeKey<Subsegment>>()] } returns subsegment
        coEvery { responseContext.proceed() } returns httpResponse
    }
}
