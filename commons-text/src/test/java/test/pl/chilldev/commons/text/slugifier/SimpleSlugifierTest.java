/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.slugifier;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.text.slugifier.SimpleSlugifier;
import pl.chilldev.commons.text.slugifier.Slugifier;

public class SimpleSlugifierTest
{
    @Test
    public void setDelimiter()
    {
        SimpleSlugifier slugifier = new SimpleSlugifier();

        slugifier.setDelimiter("::");

        Assert.assertEquals(
            "SimpleSlugifier.setDelimiter() should set delimiting sequence.",
            "foo::bar",
            slugifier.slugify("foo", "bar")
        );
    }

    @Test
    public void slugify()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should normalize the character to latin charset.",
            "rafal",
            slugifier.slugify("rąfaĺ")
        );
    }

    @Test
    public void slugifyNonAscii()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should remove all non-ascii characters.",
            "chilloutdevelopment",
            slugifier.slugify("chillout»development")
        );
    }

    @Test
    public void slugifyNonLetters()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should replace all non-letter characters with delimiter.",
            "chillout-development",
            slugifier.slugify("chillout!development")
        );
    }

    @Test
    public void slugifySpaces()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should reduce multiple delimiters to just one.",
            "chillout-development",
            slugifier.slugify("chillout - development")
        );
    }

    @Test
    public void slugifyLeading()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should drop leading delimiters.",
            "chillout-development",
            slugifier.slugify("---chillout - development")
        );
    }

    @Test
    public void slugifyTrailing()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should drop trailing delimiters.",
            "chillout-development",
            slugifier.slugify("chillout - development---")
        );
    }

    @Test
    public void slugifyLowercase()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should lowercase all letters.",
            "chillout-development",
            slugifier.slugify("ChillOut DevelopmenT")
        );
    }

    @Test
    public void slugifyMultiple()
    {
        Slugifier slugifier = new SimpleSlugifier();

        Assert.assertEquals(
            "SimpleSlugifier.slugify() should slugify all words and concatenate them with delimiter.",
            "chillout-development",
            slugifier.slugify("chillout", "development")
        );
    }
}
