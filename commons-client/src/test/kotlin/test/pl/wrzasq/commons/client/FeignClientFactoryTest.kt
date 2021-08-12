/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client

import feign.Feign
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.client.FeignClientFactory

@ExtendWith(MockKExtension::class)
class FeignClientFactoryTest {
    @MockK
    lateinit var builder: Feign.Builder

    @MockK
    lateinit var definedConfigurator: (Feign.Builder) -> Unit

    @MockK
    lateinit var customConfigurator: (Feign.Builder) -> Unit

    @Test
    fun createClient() {
        // for code coverage
        FeignClientFactory()

        val factory = FeignClientFactory(setOf(definedConfigurator)) { builder }

        every { builder.target(FeignClientFactoryTest::class.java, "foo") } returns this
        every { definedConfigurator(builder) } just runs
        every { customConfigurator(builder) } just runs

        val result: FeignClientFactoryTest = factory.createClient(
            FeignClientFactoryTest::class.java,
            "foo",
            setOf(customConfigurator)
        )

        verify { definedConfigurator(builder) }
        verify { customConfigurator(builder) }
        assertSame(this, result)
    }

    @Test
    fun createClientWithoutCustom() {
        val factory = FeignClientFactory(setOf(definedConfigurator)) { builder }

        every { builder.target(FeignClientFactoryTest::class.java, "foo") } returns this
        every { definedConfigurator(builder) } just runs
        every { customConfigurator(builder) } just runs

        val result: FeignClientFactoryTest = factory.createClient(
            FeignClientFactoryTest::class.java,
            "foo"
        )

        verify { definedConfigurator(builder) }
        verify { customConfigurator wasNot called }
        assertSame(this, result)
    }
}
