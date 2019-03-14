/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.text.slugifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.text.slugifier.SimpleSlugifier;
import pl.wrzasq.commons.text.slugifier.Slugifier;

public class SimpleSlugifierTest
{
    @Test
    public void setDelimiter()
    {
        SimpleSlugifier slugifier = new SimpleSlugifier();

        slugifier.setDelimiter("::");

        Assertions.assertEquals(
            "foo::bar",
            slugifier.slugify("foo", "bar"),
            "SimpleSlugifier.setDelimiter() should set delimiting sequence."
        );
    }

    @Test
    public void slugify()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "rafal",
            slugifier.slugify("rąfaĺ"),
            "SimpleSlugifier.slugify() should normalize the character to latin charset."
        );
    }

    @Test
    public void slugifyNonAscii()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chilloutdevelopment",
            slugifier.slugify("chillout»development"),
            "SimpleSlugifier.slugify() should remove all non-ascii characters."
        );
    }

    @Test
    public void slugifyNonLetters()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("chillout!development"),
            "SimpleSlugifier.slugify() should replace all non-letter characters with delimiter."
        );
    }

    @Test
    public void slugifySpaces()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("chillout - development"),
            "SimpleSlugifier.slugify() should reduce multiple delimiters to just one."
        );
    }

    @Test
    public void slugifyLeading()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("---chillout - development"),
            "SimpleSlugifier.slugify() should drop leading delimiters."
        );
    }

    @Test
    public void slugifyTrailing()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("chillout - development---"),
            "SimpleSlugifier.slugify() should drop trailing delimiters."
        );
    }

    @Test
    public void slugifyLowercase()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("ChillOut DevelopmenT"),
            "SimpleSlugifier.slugify() should lowercase all letters."
        );
    }

    @Test
    public void slugifyMultiple()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assertions.assertEquals(
            "chillout-development",
            slugifier.slugify("chillout", "development"),
            "SimpleSlugifier.slugify() should slugify all words and concatenate them with delimiter."
        );
    }
}
