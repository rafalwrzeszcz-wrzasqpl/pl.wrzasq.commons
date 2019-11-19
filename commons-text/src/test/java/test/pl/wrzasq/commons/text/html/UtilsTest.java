/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.html;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.text.Formatter;
import pl.wrzasq.commons.text.TextProcessingException;
import pl.wrzasq.commons.text.formatter.FormatterInterface;
import pl.wrzasq.commons.text.html.Utils;

@ExtendWith(MockitoExtension.class)
public class UtilsTest {
    @Mock
    private FormatterInterface formatHandler;

    @Test
    public void firstParagraph() {
        // just for code coverage
        new Utils();

        var value = "foo <span>bar</span>\n<em>baz</em>";
        var html = "<p>" + value + "</p> <p>\nquux</p>";

        Assertions.assertEquals(
            value,
            Utils.firstParagraph(html),
            "Utils.firstParagraph() should return content of the first paragraph from given snippet."
        );
    }

    @Test
    public void firstParagraphEmpty() {
        var value = "foo <span>bar</span>\n<em>baz</em>";

        Assertions.assertEquals(
            "",
            Utils.firstParagraph(value),
            "Utils.firstParagraph() should return empty string if there is no paragraph in given snippet."
        );
    }

    @Test
    public void truncateShortText() {
        var text = "Hello";

        Assertions.assertEquals(
            text,
            Utils.truncate(text, 7),
            "Utils.truncate() should leave text untouched if it't length fits given requirement."
        );
    }

    @Test
    public void truncate() {
        var text = "Hello world!";

        Assertions.assertEquals(
            "Hello…",
            Utils.truncate(text, 7),
            "Utils.truncate() should truncate the text and apply default suffix indicating more content with respect to word bounds."
        );
    }

    @Test
    public void truncateWordBounds() {
        var text = "Hello world!";

        Assertions.assertEquals(
            "Hello w…",
            Utils.truncate(text, 7, false),
            "Utils.truncate() should ignore word bounds if specified to."
        );
    }

    @Test
    public void truncateWordBoundsNoWord() {
        var text = "Helloworld!";

        Assertions.assertEquals(
            "Hellowo…",
            Utils.truncate(text, 7, true),
            "Utils.truncate() should ignore word bounds if a word bound can't be found."
        );
    }

    @Test
    public void truncateSuffix() {
        var text = "Hello world!";

        Assertions.assertEquals(
            "Hello.",
            Utils.truncate(text, 7, "."),
            "Utils.truncate() should use given suffix if specified."
        );
    }

    @Test
    public void truncateSuffixWordBounds() {
        var text = "Hello world!";

        Assertions.assertEquals(
            "Hello w.",
            Utils.truncate(text, 7, ".", false),
            "Utils.truncate() should use given suffix and ignore word bounds if specified to."
        );
    }

    @Test
    public void format()
        throws TextProcessingException {
        var formatter = new Formatter();
        formatter.registerFormatter("foo", this.formatHandler);

        var input = "bar";
        var result = "baz";

        Mockito.when(this.formatHandler.transform(input)).thenReturn(result);

        Utils.setFormatter(formatter);

        Assertions.assertEquals(
            result,
            Utils.format("foo", input),
            "Utils.format() should return text formatted by given format handler."
        );

        Mockito.verify(this.formatHandler).transform(input);
    }

    @Test
    public void urlEncode() {
        Assertions.assertEquals(
            "foo+bar%2F%E2%80%A6",
            Utils.urlEncode("foo bar/…"),
            "Utils.urlEncode() should build URL-suitable string from the given input."
        );
    }
}
