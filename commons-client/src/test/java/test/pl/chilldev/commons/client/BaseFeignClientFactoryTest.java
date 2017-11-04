/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client;

import java.util.Collections;

import org.junit.Test;
import pl.chilldev.commons.client.BaseFeignClientFactory;

public class BaseFeignClientFactoryTest
{
    @Test
    public void constructor()
    {
        // for now just for code coverage - maybe some day we will have something to test
        new BaseFeignClientFactory(Collections.emptyList());
    }
}
