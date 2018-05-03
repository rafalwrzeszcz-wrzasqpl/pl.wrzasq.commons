/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.html;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.text.Formatter;
import pl.chilldev.commons.text.TextProcessingException;
import pl.chilldev.commons.text.formatter.FormatterInterface;
import pl.chilldev.commons.text.html.Utils;

public class UtilsTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private FormatterInterface formatHandler;

    @Test
    public void firstParagraph()
    {
        // just for code coverage
        new Utils();

        String value = "foo <span>bar</span>\n<em>baz</em>";
        String html = "<p>" + value + "</p> <p>\nquux</p>";

        Assert.assertEquals(
            "Utils.firstParagraph() should return content of the first paragraph from given snippet.",
            value,
            Utils.firstParagraph(html)
        );
    }

    @Test
    public void firstParagraphEmpty()
    {
        String value = "foo <span>bar</span>\n<em>baz</em>";

        Assert.assertEquals(
            "Utils.firstParagraph() should return empty string if there is no paragraph in given snippet.",
            "",
            Utils.firstParagraph(value)
        );
    }

    @Test
    public void truncateShortText()
    {
        // just for code coverage
        new Utils();

        String text = "Hello";

        Assert.assertEquals(
            "Utils.truncate() should leave text untouched if it't length fits given requirement.",
            text,
            Utils.truncate(text, 7)
        );
    }

    @Test
    public void truncate()
    {
        String text = "Hello world!";

        Assert.assertEquals(
            "Utils.truncate() should truncate the text and apply default suffix indicating more content with respect to word bounds.",
            "Hello…",
            Utils.truncate(text, 7)
        );
    }

    @Test
    public void truncateWordBounds()
    {
        String text = "Hello world!";

        Assert.assertEquals(
            "Utils.truncate() should ignore word bounds if specified to.",
            "Hello w…",
            Utils.truncate(text, 7, false)
        );
    }

    @Test
    public void truncateWordBoundsNoWord()
    {
        String text = "Helloworld!";

        Assert.assertEquals(
            "Utils.truncate() should ignore word bounds if a word bound can't be found.",
            "Hellowo…",
            Utils.truncate(text, 7, true)
        );
    }

    @Test
    public void truncateSuffix()
    {
        String text = "Hello world!";

        Assert.assertEquals(
            "Utils.truncate() should use given suffix if specified.",
            "Hello.",
            Utils.truncate(text, 7, ".")
        );
    }

    @Test
    public void truncateSuffixWordBounds()
    {
        String text = "Hello world!";

        Assert.assertEquals(
            "Utils.truncate() should use given suffix and ignore word bounds if specified to.",
            "Hello w.",
            Utils.truncate(text, 7, ".", false)
        );
    }

    @Test
    public void format()
        throws TextProcessingException
    {
        Formatter formatter = new Formatter();
        formatter.registerFormatter("foo", this.formatHandler);

        String input = "bar";
        String result = "baz";

        Mockito.when(this.formatHandler.transform(input)).thenReturn(result);

        Utils.setFormatter(formatter);

        Assert.assertEquals(
            "Utils.format() should return text formatted by given format handler.",
            result,
            Utils.format("foo", input)
        );

        Mockito.verify(this.formatHandler).transform(input);
    }

    @Test
    public void urlEncode()
        throws UnsupportedEncodingException
    {
        Assert.assertEquals(
            "Utils.urlEncode() should build URL-suitable string from the given input.",
            "foo+bar%2F%E2%80%A6",
            Utils.urlEncode("foo bar/…")
        );
    }
}
