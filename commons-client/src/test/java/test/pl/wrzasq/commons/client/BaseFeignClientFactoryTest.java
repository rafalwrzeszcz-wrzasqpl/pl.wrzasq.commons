/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.client.BaseFeignClientFactory;

public class BaseFeignClientFactoryTest {
    @Test
    public void constructor() {
        // for now just for code coverage - maybe some day we will have something to test
        new BaseFeignClientFactory(Collections.emptyList());
    }
}
