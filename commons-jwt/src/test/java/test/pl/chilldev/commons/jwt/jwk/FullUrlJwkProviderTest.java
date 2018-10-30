/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jwt.jwk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.jwt.jwk.FullUrlJwkProvider;

public class FullUrlJwkProviderTest
{
    @Test
    public void urlForIssuer()
    {
        new FullUrlJwkProvider("https://chilldev.pl/test/");
    }

    @Test
    public void urlForInvalidIssuer()
    {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new FullUrlJwkProvider("blah"),
            "FullUrlJwkProvider() should throw exception when invalid URL is specified."
        );
    }
}
