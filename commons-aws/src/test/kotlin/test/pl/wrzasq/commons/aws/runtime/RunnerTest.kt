/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.runtime

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pl.wrzasq.commons.aws.runtime.Runner
import java.lang.NullPointerException

@ExperimentalSerializationApi
class RunnerTest {
    @Test
    fun mainWithoutEnvironmentVariable() {
        assertThrows<NullPointerException> { Runner.main(emptyArray()) }
    }
}
