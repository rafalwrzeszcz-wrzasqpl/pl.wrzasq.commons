/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.formatter;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.text.TextProcessingException;
import pl.chilldev.commons.text.formatter.FormatterInterface;
import pl.chilldev.commons.text.formatter.MarkdownFormatter;

public class MarkdownFormatterTest
{
    @Test
    public void transform()
        throws
            TextProcessingException
    {
        FormatterInterface textFormatter = new MarkdownFormatter();

        Assert.assertEquals(
            "MarkdownFormatter.transform() should transform Markdown source into HTML.",
            "<p>foo <strong>bar</strong></p>",
            textFormatter.transform("foo **bar**")
        );
    }
}
