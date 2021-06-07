/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.aws.runtime.StaticContext
import java.lang.System.currentTimeMillis

const val AWS_REQUEST_ID = "test"
const val LOG_GROUP_NAME = "/aws/lambda/test"
private const val LOG_STREAM_NAME = "test"
private const val FUNCTION_NAME = "Test"
const val FUNCTION_VERSION = "\$LATEST"
const val FUNCTION_ARN = "arn:aws"

class StaticContextTest {
    @Test
    fun defaultValues() {
        val logger = getLogger()

        val context = StaticContext(
            awsRequestId = AWS_REQUEST_ID,
            logGroupName = LOG_GROUP_NAME,
            logStreamName = LOG_STREAM_NAME,
            functionName = FUNCTION_NAME,
            functionVersion = FUNCTION_VERSION,
            invokedFunctionArn = FUNCTION_ARN,
            memoryLimitInMB = 1,
            logger = logger
        )

        assertEquals(AWS_REQUEST_ID, context.awsRequestId)
        assertEquals(LOG_GROUP_NAME, context.logGroupName)
        assertEquals(LOG_STREAM_NAME, context.logStreamName)
        assertEquals(FUNCTION_NAME, context.functionName)
        assertEquals(FUNCTION_VERSION, context.functionVersion)
        assertEquals(FUNCTION_ARN, context.invokedFunctionArn)
        assertNull(context.identity)
        assertNull(context.clientContext)
        assertEquals(1, context.memoryLimitInMB)
        assertSame(logger, context.logger)
    }

    @Test
    fun getRemainingTimeInMillis() {
        val deadline = 1_000_000
        val start = currentTimeMillis()

        val context = StaticContext(
            awsRequestId = AWS_REQUEST_ID,
            logGroupName = LOG_GROUP_NAME,
            logStreamName = LOG_STREAM_NAME,
            functionName = FUNCTION_NAME,
            functionVersion = FUNCTION_VERSION,
            invokedFunctionArn = FUNCTION_ARN,
            memoryLimitInMB = 1,
            runtimeDeadlineMs = start + deadline - 1,
            logger = getLogger()
        )

        assertTrue(context.remainingTimeInMillis < deadline)
    }
}
