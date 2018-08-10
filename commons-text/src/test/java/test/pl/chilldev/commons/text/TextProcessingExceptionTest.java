/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.exception;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.text.TextProcessingException;

public class TextProcessingExceptionTest
{
    @Test
    public void constructor()
    {
        TextProcessingException exception = new TextProcessingException("foo");

        Assert.assertEquals(
            "TextProcessingException() constructor should set message text.",
            "foo",
            exception.getMessage()
        );
    }

    @Test
    public void constructorWithCause()
    {
        Exception cause = new Exception();
        TextProcessingException exception = new TextProcessingException("foo", cause);

        Assert.assertEquals(
            "TextProcessingException() constructor should set message text.",
            "foo",
            exception.getMessage()
        );
        Assert.assertSame(
            "TextProcessingException() constructor should set root cause.",
            cause,
            exception.getCause()
        );
    }
}
