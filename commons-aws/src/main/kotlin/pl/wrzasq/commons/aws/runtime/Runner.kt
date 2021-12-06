/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime

/**
 * AWS Lambda entry point.
 */
object Runner {
    /**
     * Shell entry point.
     *
     * @param args Runtime arguments.
     */
    @JvmStatic
    fun main(args: Array<String>) = NativeLambdaApi.runFromFactory(System.getenv("_HANDLER"))
}
