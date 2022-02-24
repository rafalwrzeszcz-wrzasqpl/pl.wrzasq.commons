/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime

import com.amazonaws.services.lambda.runtime.Context
import java.io.InputStream
import java.io.OutputStream

/**
 * Default Lambda handler.
 */
interface RequestHandler {
    /**
     * Handles invocation.
     *
     * @param inputStream Request input.
     * @param outputStream Output stream.
     * @param context Execution context.
     */
    fun handle(inputStream: InputStream, outputStream: OutputStream, context: Context? = null)
}
