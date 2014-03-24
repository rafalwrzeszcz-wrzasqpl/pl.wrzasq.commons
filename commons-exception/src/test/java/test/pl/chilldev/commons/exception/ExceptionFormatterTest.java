/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @author Rafał Wrzeszcz <rafal.wrzeszcz@wrzasq.pl>
 * @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
 * @version 0.0.1
 * @since 0.0.1
 * @category ChillDev-Commons
 * @subcategory Exception
 */

package test.pl.chilldev.commons.exception;

import org.junit.Test;
import static org.junit.Assert.*;

import pl.chilldev.commons.exception.ExceptionFormatter;

public class ExceptionFormatterTest
{
    @Test
    public void SIMPLE_FORMAT_format()
    {
        String message = "This is test message";
        Exception error = new Exception(message);

        assertEquals(
            "ExceptionFormatter.SIMPLE_FORMAT.format() should use exception class and message.",
            "java.lang.Exception: " + message,
            ExceptionFormatter.SIMPLE_FORMAT.format(error)
        );
    }
}
