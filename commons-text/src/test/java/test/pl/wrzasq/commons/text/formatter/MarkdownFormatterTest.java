/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.formatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.text.TextProcessingException;
import pl.wrzasq.commons.text.formatter.FormatterInterface;
import pl.wrzasq.commons.text.formatter.MarkdownFormatter;

public class MarkdownFormatterTest {
    @Test
    public void transform()
        throws
            TextProcessingException {
        FormatterInterface textFormatter = new MarkdownFormatter();

        Assertions.assertEquals(
            "<p>foo <strong>bar</strong></p>",
            textFormatter.transform("foo **bar**"),
            "MarkdownFormatter.transform() should transform Markdown source into HTML."
        );
    }
}
