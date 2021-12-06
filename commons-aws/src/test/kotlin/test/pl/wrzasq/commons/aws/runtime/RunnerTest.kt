/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.runtime

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pl.wrzasq.commons.aws.runtime.Runner
import java.lang.NullPointerException

class RunnerTest {
    @Test
    fun mainWithoutEnvironmentVariable() {
        assertThrows<NullPointerException> { Runner.main(emptyArray()) }
    }
}
