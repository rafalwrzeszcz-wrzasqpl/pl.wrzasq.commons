/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorder
import com.amazonaws.xray.entities.Namespace
import com.amazonaws.xray.entities.Subsegment
import com.amazonaws.xray.entities.TraceHeader
import com.amazonaws.xray.entities.TraceHeader.SampleDecision
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.util.AttributeKey

/**
 * AWS X-Ray tracing for Ktor HTTP client.
 *
 * @param recorder X-Ray recorder.
 */
class XRayFeature(
    private val recorder: AWSXRayRecorder
) {
    // most part of it bases on aws-xray-recorder-sdk-apache-http

    private fun startSegment(request: HttpRequestBuilder): Subsegment {
        val subsegment = recorder.beginSubsegment(request.url.host)
        subsegment.setNamespace(Namespace.REMOTE.toString())

        if (subsegment.shouldPropagate()) {
            val parentSegment = subsegment.parentSegment
            val header = TraceHeader(
                parentSegment.traceId,
                if (parentSegment.isSampled) subsegment.id else null,
                if (parentSegment.isSampled) SampleDecision.SAMPLED else SampleDecision.NOT_SAMPLED
            )
            request.headers.append(TraceHeader.HEADER_KEY, header.toString())
        }

        subsegment.putHttp("request", mapOf(
            "url" to request.url.build().toString(),
            "method" to request.method.value
        ))
        return subsegment
    }

    private fun endSegment(subsegment: Subsegment, response: HttpResponse) {
        val statusCode = response.status.value
        when (statusCode / 100) {
            4 -> {
                subsegment.isError = true
                if (statusCode == HttpStatusCode.TooManyRequests.value) {
                    subsegment.isThrottle = true
                }
            }
            5 -> subsegment.isFault = true
        }

        val information = mutableMapOf<String, Any>("status" to statusCode)
        response.contentLength()?.let { information["content_length"] = it }
        subsegment.putHttp("response", information)

        recorder.endSubsegment(subsegment)
    }

    /**
     * Configuration structure.
     */
    class Config {
        /**
         * X-Ray recorder.
         */
        var recorder = AWSXRay.getGlobalRecorder()
    }

    /**
     * Plugin hook.
     */
    companion object Feature : HttpClientFeature<Config, XRayFeature> {
        override val key = AttributeKey<XRayFeature>("XRay")
        private val traceKey = AttributeKey<Subsegment>("XRaySubsegment")

        override fun prepare(block: Config.() -> Unit): XRayFeature {
            val config = Config().apply(block)
            return XRayFeature(config.recorder)
        }

        override fun install(feature: XRayFeature, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
                val subsegment = feature.startSegment(context)
                context.attributes.put(traceKey, subsegment)
                proceed()
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.State) {
                feature.endSegment(context.attributes[traceKey], context.response)
                context.attributes
                proceed()
            }
        }
    }
}
