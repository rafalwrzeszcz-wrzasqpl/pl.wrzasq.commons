/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client;

import java.util.Collection;
import java.util.function.Consumer;

import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Feign factory for standard, pure Feign.
 */
@Service
public class BaseFeignClientFactory extends FeignClientFactory
{
    /**
     * Collection of all custom client modifiers.
     *
     * @param configurators Feign builder configurators.
     */
    @Autowired(required = false)
    public BaseFeignClientFactory(Collection<Consumer<Feign.Builder>> configurators)
    {
        super(configurators, Feign::builder);
    }
}
