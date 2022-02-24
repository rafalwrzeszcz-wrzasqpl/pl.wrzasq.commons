/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.model

import com.amazonaws.services.lambda.runtime.ClientContext
import kotlinx.serialization.Serializable

/**
 * JSON implementation of client context.
 *
 * @param client Client metadata.
 * @param custom Custom context properties.
 * @param environment Environment properties.
 */
@Serializable
data class JsonClientContext(
    private val client: JsonClient,
    private val custom: Map<String, String>,
    private val environment: Map<String, String>
) : ClientContext {
    override fun getClient() = client

    override fun getCustom() = custom

    override fun getEnvironment() = environment
}
