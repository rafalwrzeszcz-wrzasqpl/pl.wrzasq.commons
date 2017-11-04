/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client;

import java.util.Collections;
import java.util.function.Consumer;

import feign.Feign;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.chilldev.commons.client.FeignClientFactory;

@RunWith(MockitoJUnitRunner.class)
public class FeignClientFactoryTest
{
    @Mock
    private Feign.Builder builder;

    @Mock
    private Consumer<Feign.Builder> definedConfigurator;

    @Mock
    private Consumer<Feign.Builder> customConfigurator;

    @Test
    public void createClient()
    {
        FeignClientFactory factory = new FeignClientFactory(
            Collections.singleton(this.definedConfigurator),
            () -> this.builder
        );

        Mockito.when(this.builder.target(FeignClientFactoryTest.class, "foo")).thenReturn(this);

        FeignClientFactoryTest result = factory.createClient(
            FeignClientFactoryTest.class,
            "foo",
            Collections.singleton(this.customConfigurator)
        );

        Mockito.verify(this.definedConfigurator).accept(this.builder);
        Mockito.verify(this.customConfigurator).accept(this.builder);

        Assert.assertSame(
            "FeignClientFactory.createClient() should return client instance created by the builder.",
            this,
            result
        );
    }

    @Test
    public void createClientWithoutCustom()
    {
        FeignClientFactory factory = new FeignClientFactory(
            Collections.singleton(this.definedConfigurator),
            () -> this.builder
        );

        Mockito.when(this.builder.target(FeignClientFactoryTest.class, "foo")).thenReturn(this);

        FeignClientFactoryTest result = factory.createClient(
            FeignClientFactoryTest.class,
            "foo"
        );

        Mockito.verify(this.definedConfigurator).accept(this.builder);
        Mockito.verifyZeroInteractions(this.customConfigurator);

        Assert.assertSame(
            "FeignClientFactory.createClient() should return client instance created by the builder.",
            this,
            result
        );
    }
}
