/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.config

import java.net.URLConnection

/**
 * Interface for accessing function environment settings.
 */
interface LambdaRuntimeConfig {
    /**
     * AWS Lambda URL endpoints root URL.
     */
    val baseUrl: String

    /**
     * CloudWatch logs group name.
     */
    val logGroupName: String

    /**
     * CloudWatch log stream name.
     */
    val logStreamName: String

    /**
     * AWS Lambda function name.
     */
    val functionName: String

    /**
     * Executed function version.
     */
    val functionVersion: String

    /**
     * Memory limit.
     */
    val memoryLimit: Int

    /**
     * HTTP connection factory.
     */
    val connectionFactory: (String) -> URLConnection

    /**
     * Critical error logger.
     */
    val errorLogger: (String) -> Unit
}
