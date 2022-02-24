/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import pl.wrzasq.commons.aws.runtime.config.EnvironmentConfig
import pl.wrzasq.commons.aws.runtime.config.LambdaRuntimeConfig
import pl.wrzasq.commons.aws.runtime.config.ResourcesFactory
import pl.wrzasq.commons.aws.runtime.model.JsonClientContext
import pl.wrzasq.commons.aws.runtime.model.JsonCognitoIdentity
import pl.wrzasq.commons.aws.runtime.model.LambdaRuntimeError
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import kotlin.reflect.full.createInstance

/**
 * Header name for request ID.
 */
const val HEADER_NAME_AWS_REQUEST_ID = "Lambda-Runtime-Aws-Request-Id"

/**
 * Header name for X-Ray trace ID.
 */
const val HEADER_NAME_TRACE_ID = "Lambda-Runtime-Trace-Id"

/**
 * Header name for function ARN.
 */
const val HEADER_NAME_INVOKED_FUNCTION_ARN = "Lambda-Runtime-Invoked-Function-Arn"

/**
 * Header name for serialized Cognito identity.
 */
const val HEADER_NAME_COGNITO_IDENTITY = "Lambda-Runtime-Cognito-Identity"

/**
 * Header name for serialized client context.
 */
const val HEADER_NAME_CLIENT_CONTEXT = "Lambda-Runtime-Client-Context"

/**
 * Header name for running deadline.
 */
const val HEADER_NAME_DEADLINE_MS = "Lambda-Runtime-Deadline-Ms"

/**
 * System property under which the X-Ray trace ID is propagated.
 */
const val PROPERTY_TRACE_ID = "com.amazonaws.xray.traceHeader"

/**
 * Lambda handler callback.
 */
typealias LambdaCallback = (InputStream, OutputStream, Context) -> Unit

/**
 * Native ("provided") Lambda API handler.
 *
 * @param json JSON serialization handler.
 * @param config Function configuration.
 */
@ExperimentalSerializationApi
class NativeLambdaApi(
    private val json: Json,
    private val config: LambdaRuntimeConfig = EnvironmentConfig()
) {
    /**
     * Runs Lambda native handler.
     *
     * @param handler Lambda entry point.
     */
    fun run(handler: LambdaCallback) = runBlocking {
        try {
            while (true) {
                val requestConnection = config.connectionFactory("${config.baseUrl}invocation/next")
                withContext(Dispatchers.IO) {
                    requestConnection.getInputStream()
                }.use {
                    val requestId = requestConnection.getHeaderField(HEADER_NAME_AWS_REQUEST_ID)

                    // we handle both cases to clean up previous execution
                    val traceId = requestConnection.getHeaderField(HEADER_NAME_TRACE_ID)
                    if (traceId.isNullOrEmpty()) {
                        System.clearProperty(PROPERTY_TRACE_ID)
                    } else {
                        System.setProperty(PROPERTY_TRACE_ID, traceId)
                    }

                    try {
                        sendResponse("${config.baseUrl}invocation/${requestId}/response") { output ->
                            handler(it, output, buildContext(requestId, requestConnection as HttpURLConnection))
                        }
                    } catch (error: Exception) {
                        // TODO: handle case when Lambda handler itself returns LambdaRuntimeError
                        sendErrorResponse(
                            "${config.baseUrl}invocation/${requestId}/error",
                            "Failed to run Lambda.",
                            error
                        )
                    }
                }
            }
        } catch (error: Exception) {
            try {
                sendErrorResponse("${config.baseUrl}init/error", "Failed to initialize Lambda.", error)
            } catch (innerError: Exception) {
                logError("Failed to report init error.", innerError)
            }
        }
    }

    private fun sendResponse(url: String, handler: (OutputStream) -> Unit) {
        val responseConnection = config.connectionFactory(url) as HttpURLConnection
        responseConnection.doOutput = true
        responseConnection.requestMethod = "POST"
        handler(responseConnection.outputStream)
        while (responseConnection.inputStream.read() != -1) {
            // drain
        }
    }

    private fun sendErrorResponse(url: String, message: String, error: Exception) {
        logError(message, error)

        sendResponse(url) {
            json.encodeToStream(
                LambdaRuntimeError.serializer(),
                LambdaRuntimeError(
                    error.javaClass.name,
                    error.message ?: message,
                    error.stackTrace.map(StackTraceElement::toString)
                ),
                it
            )
        }
    }

    private fun logError(message: String, error: Exception) {
        config.errorLogger(message)
        config.errorLogger(error.stackTraceToString())
    }

    private fun buildContext(awsRequestId: String, request: HttpURLConnection): Context = StaticContext(
        awsRequestId = awsRequestId,
        logGroupName = config.logGroupName,
        logStreamName = config.logStreamName,
        functionName = config.functionName,
        functionVersion = config.functionVersion,
        invokedFunctionArn = request.getHeaderField(HEADER_NAME_INVOKED_FUNCTION_ARN),
        cognitoIdentity = request.getHeaderField(HEADER_NAME_COGNITO_IDENTITY)
            ?.let<String, JsonCognitoIdentity>(json::decodeFromString),
        clientContext = request.getHeaderField(HEADER_NAME_CLIENT_CONTEXT)
            ?.let<String, JsonClientContext>(json::decodeFromString),
        runtimeDeadlineMs = request.getHeaderField(HEADER_NAME_DEADLINE_MS)?.let(String::toLong) ?: 0,
        memoryLimitInMB = config.memoryLimit,
        logger = LambdaRuntime.getLogger()
    )

    companion object {
        /**
         * Reflection entry point.
         *
         * @param handler Factory type name.
         */
        fun runFromFactory(handler: String) {
            val factory = Class.forName(handler).kotlin.createInstance()
            if (factory is ResourcesFactory) {
                factory.lambdaApi.run(factory.lambdaCallback)
            } else {
                throw RuntimeException("$handler is not a factory for Lambda resources")
            }
        }
    }
}
