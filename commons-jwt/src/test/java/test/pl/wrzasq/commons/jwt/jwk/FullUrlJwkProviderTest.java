/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.jwt.jwk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.jwt.jwk.FullUrlJwkProvider;

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
