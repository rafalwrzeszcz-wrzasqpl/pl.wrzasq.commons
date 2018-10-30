/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.formatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.text.TextProcessingException;
import pl.chilldev.commons.text.formatter.FormatterInterface;
import pl.chilldev.commons.text.formatter.PlainTextFormatter;

public class PlainTextFormatterTest
{
    @Test
    public void transform()
        throws
            TextProcessingException
    {
        FormatterInterface textFormatter = new PlainTextFormatter();

        Assertions.assertEquals(
            "&lt;span&gt;foo<br/>&lt;/span&gt;",
            textFormatter.transform("<span>foo\n</span>"),
            "PlainTextFormatter.transform() should return escaped text that resembles plain text representation with line breaks as new lines."
        );
    }
}
