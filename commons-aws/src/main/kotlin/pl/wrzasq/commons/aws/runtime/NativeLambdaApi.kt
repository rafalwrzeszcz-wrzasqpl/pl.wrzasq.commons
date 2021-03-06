/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaRuntime
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import pl.wrzasq.commons.aws.runtime.config.EnvironmentConfig
import pl.wrzasq.commons.aws.runtime.config.LambdaRuntimeConfig
import pl.wrzasq.commons.aws.runtime.model.LambdaRuntimeError
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.HttpURLConnection

/**
 * Header name for request ID.
 */
const val HEADER_NAME_AWS_REQUEST_ID = "Lambda-Runtime-Aws-Request-Id"

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
 * Native ("provided") Lambda API handler.
 *
 * @param objectMapper JSON serialization handler.
 * @param config Function configuration.
 */
class NativeLambdaApi(
    private val objectMapper: ObjectMapper,
    private val config: LambdaRuntimeConfig = EnvironmentConfig()
) {
    /**
     * Runs Lambda native handler.
     *
     * @param handler Lambda entry point.
     */
    fun run(handler: (InputStream, OutputStream, Context) -> Unit) = runBlocking {
        try {
            while (true) {
                val requestConnection = config.connectionFactory("${config.baseUrl}invocation/next")
                requestConnection.getInputStream().use {
                    val requestId = requestConnection.getHeaderField(HEADER_NAME_AWS_REQUEST_ID)
                    try {
                        sendResponse("${config.baseUrl}invocation/${requestId}/response") { output ->
                            handler(it, output, buildContext(requestId, requestConnection as HttpURLConnection))
                        }
                    } catch (error: Exception) {
                        // TODO: handle X-Ray tracing ID to handle propagation
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
            objectMapper.writeValue(
                it,
                LambdaRuntimeError(
                    error.javaClass.name,
                    error.message ?: message,
                    error.stackTrace.map(StackTraceElement::toString)
                )
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
            ?.let<String, CognitoIdentity>(objectMapper::readValue),
        clientContext = request.getHeaderField(HEADER_NAME_CLIENT_CONTEXT)
            ?.let<String, ClientContext>(objectMapper::readValue),
        runtimeDeadlineMs = request.getHeaderField(HEADER_NAME_DEADLINE_MS)?.let(String::toLong) ?: 0,
        memoryLimitInMB = config.memoryLimit,
        logger = LambdaRuntime.getLogger()
    )
}
