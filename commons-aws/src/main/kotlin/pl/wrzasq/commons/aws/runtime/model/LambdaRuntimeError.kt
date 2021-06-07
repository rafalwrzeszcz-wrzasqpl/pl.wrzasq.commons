/**
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.model

/**
 * Lambda error response structure.
 *
 * @param errorType Error type.
 * @param errorMessage Error description.
 * @param stackTrace Debug info.
 */
data class LambdaRuntimeError(
    val errorType: String,
    val errorMessage: String,
    val stackTrace: List<String> = emptyList()
)
