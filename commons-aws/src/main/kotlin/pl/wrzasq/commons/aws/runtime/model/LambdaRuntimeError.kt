/**
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.model

import kotlinx.serialization.Serializable

/**
 * Lambda error response structure.
 *
 * @param errorType Error type.
 * @param errorMessage Error description.
 * @param stackTrace Debug info.
 */
@Serializable
data class LambdaRuntimeError(
    val errorType: String,
    val errorMessage: String,
    val stackTrace: List<String> = emptyList()
)
