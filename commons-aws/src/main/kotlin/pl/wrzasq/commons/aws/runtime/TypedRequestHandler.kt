/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Default Lambda handler.
 *
 * @param json JSON (de)serialization handler.
 * @param requestSerializer Serializer of <RequestType>.
 * @param responseSerializer Serializer of <ResponseType>.
 * @param <RequestType> Type of handled request.
 * @param <ResponseType> Type of produced response.
 */
@ExperimentalSerializationApi
abstract class TypedRequestHandler<RequestType, ResponseType>(
    private val json: Json,
    private val requestSerializer: KSerializer<RequestType>,
    private val responseSerializer: KSerializer<ResponseType>
) : RequestHandler {
    override fun handle(inputStream: InputStream, outputStream: OutputStream, context: Context?) {
        val request = json.decodeFromStream(requestSerializer, inputStream)
        val response = handle(request, context)
        json.encodeToStream(responseSerializer, response, outputStream)
    }

    /**
     * Handles invocation.
     *
     * @param request Request payload.
     * @param context Execution context.
     * @return Produced response.
     */
    abstract fun handle(request: RequestType, context: Context?): ResponseType
}
