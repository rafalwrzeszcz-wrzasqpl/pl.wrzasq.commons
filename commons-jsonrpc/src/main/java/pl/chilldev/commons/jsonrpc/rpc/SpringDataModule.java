/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;

/**
 * Spring-Data types handling module.
 */
public class SpringDataModule implements DispatcherModule
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeIntrospector(Introspector introspector)
    {
        // parameters retrievers

        // Spring Data paged request retriever
        introspector.registerParameterProvider(
            Pageable.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return params.getPageable(Integer.parseInt(defaultValue));
            }
        );

        // return types handlers

        // Spring Data paged response handler
        introspector.registerResultMapper(
            Page.class,
            (Page page) -> {
                Map<String, Object> result = new HashMap<>();
                result.put("count", page.getTotalElements());
                result.put("records", page.getContent());
                return result;
            }
        );
    }
}
