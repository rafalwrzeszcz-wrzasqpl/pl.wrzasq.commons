/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.slugifier;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.text.slugifier.Slugifier;

public class SlugifierTest
{
    @Test
    public void slugify()
    {
        Slugifier slugifier = new Slugifier()
        {
            @Override
            public String slugify(String... texts)
            {
                return "foo";
            }
        };

        Assert.assertEquals(
            "Slugifier.slugify() should by default apply slugifier logic on sinle-elemene array.",
            "foo",
            slugifier.slugify("test")
        );
    }
}
