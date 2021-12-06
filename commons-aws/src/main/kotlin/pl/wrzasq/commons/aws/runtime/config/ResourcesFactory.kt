/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.config

import pl.wrzasq.commons.aws.runtime.LambdaCallback
import pl.wrzasq.commons.aws.runtime.NativeLambdaApi

/**
 * Factory interface for Lambda runtime resources.
 */
interface ResourcesFactory {
    /**
     * Native AWS Lambda runtime handler.
     */
    val lambdaApi: NativeLambdaApi

    /**
     * Lambda handler callback.
     */
    val lambdaCallback: LambdaCallback
}
