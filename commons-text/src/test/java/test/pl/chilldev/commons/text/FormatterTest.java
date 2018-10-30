/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.text.Formatter;
import pl.chilldev.commons.text.TextProcessingException;
import pl.chilldev.commons.text.formatter.HtmlFormatter;

public class FormatterTest
{
    @Test
    public void transform()
        throws
            TextProcessingException
    {
        Formatter formatter = new Formatter();
        formatter.registerFormatter("html", new HtmlFormatter());

        Assertions.assertEquals(
            "<p>foo</p>",
            formatter.transform("html", "<p>foo</p>"),
            "Formatter.transform() should handle source text of given format."
        );
    }

    @Test
    public void transformUnknownException()
        throws
            TextProcessingException
    {
        Formatter formatter = new Formatter();

        Assertions.assertThrows(
            TextProcessingException.class,
            () -> formatter.transform("html", "<p>foo</p>"),
            "Formatter.transform() should throw exception when unknown format is requested."
        );
    }
}
