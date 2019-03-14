/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client;

import java.util.Collections;
import java.util.function.Consumer;

import feign.Feign;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.client.FeignClientFactory;

@ExtendWith(MockitoExtension.class)
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

        Assertions.assertSame(
            this,
            result,
            "FeignClientFactory.createClient() should return client instance created by the builder."
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

        Assertions.assertSame(
            this,
            result,
            "FeignClientFactory.createClient() should return client instance created by the builder."
        );
    }
}
