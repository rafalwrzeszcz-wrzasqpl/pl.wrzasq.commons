/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import feign.Feign;

/**
 * Generic factory for REST services clients.
 */
public class FeignClientFactory {
    /**
     * Collection of all custom client modifiers.
     */
    private Collection<Consumer<Feign.Builder>> configurators;

    /**
     * Source for Feign builder.
     */
    private Supplier<Feign.Builder> feignBuilderSource;

    /**
     * Collection of all custom client modifiers.
     *
     * @param configurators Feign builder configurators.
     * @param feignBuilderSource Feign builder producer.
     */
    public FeignClientFactory(
        Collection<Consumer<Feign.Builder>> configurators,
        Supplier<Feign.Builder> feignBuilderSource
    ) {
        this.configurators = configurators;
        this.feignBuilderSource = feignBuilderSource;
    }

    /**
     * Creates Feign client of given type.
     *
     * @param clientType Client type class.
     * @param url Client URL.
     * @param configurators Custom configuration callbacks.
     * @param <ClientType> Type of client.
     * @return Client instance.
     */
    public <ClientType> ClientType createClient(
        Class<? extends ClientType> clientType,
        String url,
        Collection<Consumer<Feign.Builder>> configurators
    ) {
        Feign.Builder builder = this.feignBuilderSource.get();

        // pre-defined configurators
        this.configurators.forEach((Consumer<Feign.Builder> configurator) -> configurator.accept(builder));
        // custom configurators
        configurators.forEach((Consumer<Feign.Builder> configurator) -> configurator.accept(builder));

        return builder.target(clientType, url);
    }

    /**
     * Default creator for Feign client.
     *
     * @param clientType Client type class.
     * @param url Client URL.
     * @param <ClientType> Type of client.
     * @return Client instance.
     */
    public <ClientType> ClientType createClient(
        Class<? extends ClientType> clientType,
        String url
    ) {
        return this.createClient(clientType, url, Collections.emptyList());
    }
}
