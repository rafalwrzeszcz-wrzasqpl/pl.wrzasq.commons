/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client

import feign.Feign

/**
 * Generic factory for REST services clients.
 *
 * @property configurators Feign builder configurators.
 * @property feignBuilderSource Feign builder producer.
 */
class FeignClientFactory(
    private val configurators: Collection<(Feign.Builder) -> Unit> = emptyList(),
    private val feignBuilderSource: () -> Feign.Builder = Feign::builder
) {
    /**
     * Creates Feign client of given type.
     *
     * @param clientType Client type class.
     * @param url Client URL.
     * @param configurators Custom configuration callbacks.
     * @param <ClientType> Type of client.
     * @return Client instance.
     */
    fun <ClientType> createClient(
        clientType: Class<out ClientType>,
        url: String,
        configurators: Collection<(Feign.Builder) -> Unit> = emptyList()
    ): ClientType {
        val builder = feignBuilderSource()

        // pre-defined configurators
        this.configurators.forEach { it(builder) }
        // custom configurators
        configurators.forEach { it(builder) }

        return builder.target(clientType, url)
    }
}
