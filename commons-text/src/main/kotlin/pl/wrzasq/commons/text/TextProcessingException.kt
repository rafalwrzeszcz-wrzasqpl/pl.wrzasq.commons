/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text

import java.lang.Exception

/**
 * Error occurring during text processing.
 */
class TextProcessingException : Exception {
    /**
     * Constructor with error message.
     *
     * @param message Error message.
     */
    constructor(message: String) : super(message)

    /**
     * Constructor with error message and root cause.
     *
     * @param message Error message.
     * @param cause The underlying issue that caused the problem.
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
}
