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
import com.amazonaws.services.lambda.runtime.LambdaLogger
import java.lang.System.currentTimeMillis

/**
 * Value-based implementation of Lambda context.
 */
data class StaticContext(
    private val awsRequestId: String,
    private val logGroupName: String,
    private val logStreamName: String,
    private val functionName: String,
    private val functionVersion: String,
    private val invokedFunctionArn: String,
    private var cognitoIdentity: CognitoIdentity? = null,
    private var clientContext: ClientContext? = null,
    private var runtimeDeadlineMs: Long = 0,
    private val memoryLimitInMB: Int,
    private val logger: LambdaLogger
) : Context {
    override fun getAwsRequestId() = awsRequestId

    override fun getLogGroupName() = logGroupName

    override fun getLogStreamName() = logStreamName

    override fun getFunctionName() = functionName

    override fun getFunctionVersion() = functionVersion

    override fun getInvokedFunctionArn() = invokedFunctionArn

    override fun getIdentity() = cognitoIdentity

    override fun getClientContext() = clientContext

    override fun getRemainingTimeInMillis() = (runtimeDeadlineMs - currentTimeMillis()).toInt()

    override fun getMemoryLimitInMB() = memoryLimitInMB

    override fun getLogger() = logger
}
