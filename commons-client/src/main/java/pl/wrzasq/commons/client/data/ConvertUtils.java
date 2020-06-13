/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedModel;

/**
 * Conversion utils for Spring Data collections.
 */
public class ConvertUtils {
    /**
     * Extracts URL string values from sorting model.
     *
     * @param sort Spring Data sort model.
     * @return URL values.
     */
    public static Collection<String> extractSort(Sort sort) {
        if (sort == null) {
            return null;
        }

        return sort.stream()
            .map(criteria -> criteria.getProperty() + "," + criteria.getDirection().name())
            .collect(Collectors.toList());
    }

    /**
     * Converts HATEOAS resources into Spring Data page.
     *
     * @param resources Paged resources.
     * @param request Pagination specification.
     * @param <ResourceType> Collection element type.
     * @return Paged result.
     */
    public static <ResourceType> Page<ResourceType> buildPageFromResources(
        PagedModel<? extends ResourceType> resources,
        Pageable request
    ) {
        return new PageImpl<>(
            new ArrayList<>(resources.getContent()),
            request,
            Optional.ofNullable(resources.getMetadata()).map(PagedModel.PageMetadata::getTotalElements).orElse(0L)
        );
    }
}
