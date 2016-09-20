/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(
            "Formatter.transform() should handle source text of given format.",
            "<p>foo</p>",
            formatter.transform("html", "<p>foo</p>")
        );
    }

    @Test(expected = TextProcessingException.class)
    public void transformUnknownException()
        throws
            TextProcessingException
    {
        Formatter formatter = new Formatter();

        formatter.transform("html", "<p>foo</p>");
    }
}
