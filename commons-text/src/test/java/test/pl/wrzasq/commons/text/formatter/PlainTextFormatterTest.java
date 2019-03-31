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
import pl.wrzasq.commons.text.formatter.PlainTextFormatter;

public class PlainTextFormatterTest {
    @Test
    public void transform()
        throws
            TextProcessingException {
        FormatterInterface textFormatter = new PlainTextFormatter();

        Assertions.assertEquals(
            "&lt;span&gt;foo<br/>&lt;/span&gt;",
            textFormatter.transform("<span>foo\n</span>"),
            "PlainTextFormatter.transform() should return escaped text that resembles plain text representation with line breaks as new lines."
        );
    }
}
