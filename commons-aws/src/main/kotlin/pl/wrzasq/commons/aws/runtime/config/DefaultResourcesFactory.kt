/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import pl.wrzasq.commons.aws.runtime.RequestHandler
import pl.wrzasq.commons.aws.runtime.NativeLambdaApi

/**
 * Factory interface for Lambda runtime resources.
 *
 * @param json JSON (de)serialization handler.
 */
@ExperimentalSerializationApi
abstract class DefaultResourcesFactory(
    json: Json = Json.Default
) : ResourcesFactory {
    override val lambdaApi = NativeLambdaApi(json)

    /**
     * Lambda handler object.
     */
    protected abstract val handler: RequestHandler

    override val lambdaCallback
        get() = handler::handle
}
