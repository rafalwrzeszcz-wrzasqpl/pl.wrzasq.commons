/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.html;

import org.junit.Test;
import static org.junit.Assert.*;

import pl.chilldev.commons.text.html.Utils;

public class UtilsTest
{
    @Test
    public void firstParagraph()
    {
        String value = "foo <span>bar</span>\n<em>baz</em>";
        String html = "<p>" + value + "</p> <p>\nquux</p>";

        assertEquals(
            "Utils.firstParagraph() should return content of the first paragraph from given snippet.",
            value,
            Utils.firstParagraph(html)
        );
    }

    @Test
    public void firstParagraphEmpty()
    {
        String value = "foo <span>bar</span>\n<em>baz</em>";

        assertEquals(
            "Utils.firstParagraph() should return empty string if there is no paragraph in given snippet.",
            "",
            Utils.firstParagraph(value)
        );
    }
}
