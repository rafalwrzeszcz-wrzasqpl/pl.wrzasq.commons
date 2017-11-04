/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jwt.jwk;

import org.junit.Test;
import pl.chilldev.commons.jwt.jwk.FullUrlJwkProvider;

public class FullUrlJwkProviderTest
{
    @Test
    public void urlForIssuer()
    {
        new FullUrlJwkProvider("https://chilldev.pl/test/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlForInvalidIssuer()
    {
        new FullUrlJwkProvider("blah");
    }
}
