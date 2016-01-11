/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;

/**
 * Spring-Data types handling module.
 */
public class SpringDataModule implements ClientModule
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeIntrospector(Introspector introspector)
    {
        // parameters mappers

        // Spring Data page request mapper
        introspector.registerParameterMapper(
            Pageable.class,
            (String name, Pageable value, Map<String, Object> params) -> {
                params.put("page", value.getPageNumber());
                params.put("limit", value.getPageSize());
                params.put("sort", value.getSort());
            }
        );
    }
}
