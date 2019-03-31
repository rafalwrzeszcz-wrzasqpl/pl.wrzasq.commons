/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text;

/**
 * Error occuring during text processing.
 */
public class TextProcessingException extends Exception {
    /**
     * Serializable class ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with error message.
     *
     * @param message Error message.
     */
    public TextProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and root cause.
     *
     * @param message Error message.
     * @param cause The underlying issue that caused the problem.
     */
    public TextProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
