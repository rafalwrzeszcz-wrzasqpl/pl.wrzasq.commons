/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_AWS_REQUEST_ID
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_CLIENT_CONTEXT
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_COGNITO_IDENTITY
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_DEADLINE_MS
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_INVOKED_FUNCTION_ARN
import pl.wrzasq.commons.aws.runtime.HEADER_NAME_TRACE_ID
import pl.wrzasq.commons.aws.runtime.NativeLambdaApi
import pl.wrzasq.commons.aws.runtime.PROPERTY_TRACE_ID
import pl.wrzasq.commons.aws.runtime.config.LambdaRuntimeConfig
import pl.wrzasq.commons.aws.runtime.model.LambdaRuntimeError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URLConnection

private const val TRACE_ID = "1234abcd"
private const val BASE_URL = "http://localhost/"
private const val HTTP_METHOD = "POST"
private const val CONTENT = "{}"

private const val URL_NEXT_REQUEST = "${BASE_URL}invocation/next"
private const val URL_RESPONSE = "${BASE_URL}invocation/${AWS_REQUEST_ID}/response"
private const val URL_INIT_ERROR = "${BASE_URL}init/error"

@ExtendWith(MockKExtension::class)
class NativeLambdaApiTest {
    @MockK
    lateinit var objectMapper: ObjectMapper

    @MockK
    lateinit var requestConnection: HttpURLConnection

    @MockK
    lateinit var responseConnection: HttpURLConnection

    @MockK
    lateinit var errorLogger: (String) -> Unit

    @MockK
    lateinit var connectionFactory: (String) -> URLConnection

    @MockK
    lateinit var config: LambdaRuntimeConfig

    @MockK
    lateinit var handler: (InputStream, OutputStream, Context) -> Unit

    @MockK
    lateinit var cognitoIdentity: CognitoIdentity

    @MockK
    lateinit var clientContext: ClientContext

    @BeforeEach
    fun setUp() {
        every { config.baseUrl } returns BASE_URL
        every { config.connectionFactory } returns connectionFactory
        every { config.errorLogger } returns errorLogger
    }

    @Test
    fun constructor() {
        // can not test flow, but just instantiation of default values
        NativeLambdaApi(objectMapper)
    }

    @Test
    fun run() {
        // we need to set it upfront to make sure it's cleared in this case - we mock no header
        System.setProperty(PROPERTY_TRACE_ID, TRACE_ID)

        every {
            connectionFactory(URL_NEXT_REQUEST)
        } returns requestConnection andThenThrows RuntimeException() // second invocation to end loop
        every { connectionFactory(URL_RESPONSE) } returns responseConnection
        every { errorLogger(any()) } just runs

        val input = ByteArrayInputStream(CONTENT.toByteArray())

        every { requestConnection.inputStream } returns input
        every { requestConnection.getHeaderField(HEADER_NAME_AWS_REQUEST_ID) } returns AWS_REQUEST_ID
        every { requestConnection.getHeaderField(HEADER_NAME_TRACE_ID) } returns null
        every { requestConnection.getHeaderField(HEADER_NAME_INVOKED_FUNCTION_ARN) } returns FUNCTION_ARN
        every { requestConnection.getHeaderField(HEADER_NAME_COGNITO_IDENTITY) } returns null
        every { requestConnection.getHeaderField(HEADER_NAME_CLIENT_CONTEXT) } returns null
        every { requestConnection.getHeaderField(HEADER_NAME_DEADLINE_MS) } returns "100"

        every { config.logGroupName } returns LOG_GROUP_NAME
        every { config.logStreamName } returns ""
        every { config.functionName } returns AWS_REQUEST_ID
        every { config.functionVersion } returns FUNCTION_VERSION
        every { config.memoryLimit } returns 0

        val output = ByteArrayOutputStream()

        every { responseConnection.doOutput = any() } just runs
        every { responseConnection.requestMethod = HTTP_METHOD } just runs
        every { responseConnection.inputStream } returns "".byteInputStream()
        every { responseConnection.outputStream } returns output

        every { handler(input, output, any()) } just runs

        val runtime = NativeLambdaApi(objectMapper, config)
        runtime.run(handler)

        val context = CapturingSlot<Context>()

        verify { handler(input, output, capture(context)) }
        verify { responseConnection.requestMethod = HTTP_METHOD }

        assertNull(context.captured.identity)
        assertNull(context.captured.clientContext)
        assertNull(System.getProperty(PROPERTY_TRACE_ID))
    }

    @Test
    fun runWithIdentity() {
        every {
            connectionFactory(URL_NEXT_REQUEST)
        } returns requestConnection andThenThrows RuntimeException() // second invocation to end loop
        every { connectionFactory(URL_RESPONSE) } returns responseConnection
        every { errorLogger(any()) } just runs

        val input = ByteArrayInputStream(CONTENT.toByteArray())

        val cognitoIdentityJson = "{}"
        val clientContextJson = "{:}"

        every { requestConnection.inputStream } returns input
        every { requestConnection.getHeaderField(HEADER_NAME_AWS_REQUEST_ID) } returns AWS_REQUEST_ID
        every { requestConnection.getHeaderField(HEADER_NAME_TRACE_ID) } returns TRACE_ID
        every { requestConnection.getHeaderField(HEADER_NAME_INVOKED_FUNCTION_ARN) } returns FUNCTION_ARN
        every { requestConnection.getHeaderField(HEADER_NAME_COGNITO_IDENTITY) } returns cognitoIdentityJson
        every { requestConnection.getHeaderField(HEADER_NAME_CLIENT_CONTEXT) } returns clientContextJson
        every { requestConnection.getHeaderField(HEADER_NAME_DEADLINE_MS) } returns null

        every { config.logGroupName } returns LOG_GROUP_NAME
        every { config.logStreamName } returns ""
        every { config.functionName } returns AWS_REQUEST_ID
        every { config.functionVersion } returns FUNCTION_VERSION
        every { config.memoryLimit } returns 0

        every {
            objectMapper.readValue(cognitoIdentityJson, any<TypeReference<CognitoIdentity>>())
        } returns cognitoIdentity
        every { objectMapper.readValue(clientContextJson, any<TypeReference<ClientContext>>()) } returns clientContext

        val output = ByteArrayOutputStream()

        every { responseConnection.doOutput = any() } just runs
        every { responseConnection.requestMethod = HTTP_METHOD } just runs
        every { responseConnection.inputStream } returns "".byteInputStream()
        every { responseConnection.outputStream } returns output

        every { handler(input, output, any()) } just runs

        val runtime = NativeLambdaApi(objectMapper, config)
        runtime.run(handler)

        val context = CapturingSlot<Context>()

        verify { handler(input, output, capture(context)) }
        verify { responseConnection.requestMethod = HTTP_METHOD }

        assertSame(cognitoIdentity, context.captured.identity)
        assertSame(clientContext, context.captured.clientContext)
        assertEquals(TRACE_ID, System.getProperty(PROPERTY_TRACE_ID))
    }

    @Test
    fun runError() {
        every {
            connectionFactory(URL_NEXT_REQUEST)
        } returns requestConnection andThenThrows RuntimeException() // second invocation to end loop
        every { connectionFactory("${BASE_URL}invocation/${AWS_REQUEST_ID}/error") } returns responseConnection
        every { connectionFactory(URL_RESPONSE) } throws RuntimeException()
        every { errorLogger(any()) } just runs

        val input = ByteArrayInputStream(CONTENT.toByteArray())

        every { requestConnection.inputStream } returns input
        every { requestConnection.getHeaderField(HEADER_NAME_AWS_REQUEST_ID) } returns AWS_REQUEST_ID
        every { requestConnection.getHeaderField(HEADER_NAME_TRACE_ID) } returns ""

        val output = ByteArrayOutputStream()

        every { objectMapper.writeValue(output, any<LambdaRuntimeError>()) } just runs

        every { responseConnection.doOutput = any() } just runs
        every { responseConnection.requestMethod = HTTP_METHOD } just runs
        every { responseConnection.inputStream } returns "".byteInputStream()
        every { responseConnection.outputStream } returns output

        val runtime = NativeLambdaApi(objectMapper, config)
        runtime.run(handler)

        verify { objectMapper.writeValue(output, any<LambdaRuntimeError>()) }
        verify { responseConnection.requestMethod = HTTP_METHOD }
        verify { errorLogger(any()) }
    }

    @Test
    fun runInitError() {
        every { connectionFactory(URL_NEXT_REQUEST) } throws RuntimeException("PANIC")
        every { connectionFactory(URL_INIT_ERROR) } returns responseConnection
        every { errorLogger(any()) } just runs

        val output = ByteArrayOutputStream()

        every { objectMapper.writeValue(output, any<LambdaRuntimeError>()) } just runs

        every { responseConnection.doOutput = any() } just runs
        every { responseConnection.requestMethod = HTTP_METHOD } just runs
        every { responseConnection.inputStream } returns "{}".byteInputStream()
        every { responseConnection.outputStream } returns output

        val runtime = NativeLambdaApi(objectMapper, config)
        runtime.run(handler)

        verify { objectMapper.writeValue(output, any<LambdaRuntimeError>()) }
        verify { responseConnection.requestMethod = HTTP_METHOD }
        verify { errorLogger(any()) }
    }

    @Test
    fun runInitAndCatchError() {
        every { connectionFactory(any()) } throws RuntimeException()
        every { errorLogger(any()) } just runs

        val runtime = NativeLambdaApi(objectMapper, config)
        runtime.run(handler)

        verify { connectionFactory(URL_NEXT_REQUEST) }
        verify { connectionFactory(URL_INIT_ERROR) }
        verify { errorLogger(any()) }
    }
}
