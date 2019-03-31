/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.text.TextProcessingException;

public class TextProcessingExceptionTest {
    @Test
    public void constructor() {
        TextProcessingException exception = new TextProcessingException("foo");

        Assertions.assertEquals(
            "foo",
            exception.getMessage(),
            "TextProcessingException() constructor should set message text."
        );
    }

    @Test
    public void constructorWithCause() {
        Exception cause = new Exception();
        TextProcessingException exception = new TextProcessingException("foo", cause);

        Assertions.assertEquals(
            "foo",
            exception.getMessage(),
            "TextProcessingException() constructor should set message text."
        );
        Assertions.assertSame(
            cause,
            exception.getCause(),
            "TextProcessingException() constructor should set root cause."
        );
    }
}
