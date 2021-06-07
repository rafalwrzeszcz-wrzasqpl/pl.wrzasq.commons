/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.config

import java.lang.System.getenv
import java.net.URL
import java.net.URLConnection

private fun defaultConnectionFactory(url: String): URLConnection = URL(url).openConnection()

/**
 * AWS Lambda environment variables configuration.
 */
class EnvironmentConfig : LambdaRuntimeConfig {
    private val runtimeApi by lazy { getenv("AWS_LAMBDA_RUNTIME_API") }

    override val baseUrl: String by lazy { "http://${runtimeApi}/2018-06-01/runtime/" }
    override val logGroupName: String by lazy { getenv("AWS_LAMBDA_LOG_GROUP_NAME") }
    override val logStreamName: String by lazy { getenv("AWS_LAMBDA_LOG_STREAM_NAME") }
    override val functionName: String by lazy { getenv("AWS_LAMBDA_FUNCTION_NAME") }
    override val functionVersion: String by lazy { getenv("AWS_LAMBDA_FUNCTION_VERSION") }
    override val memoryLimit: Int by lazy { getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE").toIntOrNull() ?: 0 }
    override val connectionFactory: (String) -> URLConnection = ::defaultConnectionFactory
    override val errorLogger: (String) -> Unit = ::println
}
