/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.formatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.text.formatter.HtmlFormatter;

public class HtmlFormatterTest {
    @Test
    public void transform() {
        var textFormatter = new HtmlFormatter();

        Assertions.assertEquals(
            "<span>foo</span>",
            textFormatter.transform("<span>foo</span>"),
            "HtmlFormatter.transform() should return untouched (X)HTML snippet."
        );
    }
}
