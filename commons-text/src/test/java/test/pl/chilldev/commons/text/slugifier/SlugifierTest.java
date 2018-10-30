/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.text.slugifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

        Assertions.assertEquals(
            "foo",
            slugifier.slugify("test"),
            "Slugifier.slugify() should by default apply slugifier logic on sinle-elemene array."
        );
    }
}
