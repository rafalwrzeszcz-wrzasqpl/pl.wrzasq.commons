/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import pl.wrzasq.commons.data.ConvertUtils;

public class ConvertUtilsTest {
    @Test
    public void extractSort() {
        // just for code coverage
        new ConvertUtils();

        Collection<String> extracted = ConvertUtils.extractSort(
            Sort.by(
                new Sort.Order(Sort.Direction.DESC, "id"),
                new Sort.Order(Sort.Direction.ASC, "name")
            )
        );

        Assertions.assertEquals(
            2,
            extracted.size(),
            "ConvertUtils.extractSort() should return collection of all sort params."
        );

        Assertions.assertTrue(
            extracted.contains("id,DESC"),
            "ConvertUtils.extractSort() should return collection of all sort params."
        );
        Assertions.assertTrue(
            extracted.contains("name,ASC"),
            "ConvertUtils.extractSort() should return collection of all sort params."
        );
    }

    @Test
    public void extractSortNull() {
        Assertions.assertNull(
            ConvertUtils.extractSort(null),
            "ConvertUtils.extractSort() should return NULL value for NULL input."
        );
    }

    @Test
    public void buildPageFromResources() {
        Object object1 = new Object();
        Object object2 = new Object();
        int size = 2;
        int number = 1;
        int total = 10;

        PagedResources<Object> resources = new PagedResources<>(
            Stream.of(object1, object2).collect(Collectors.toList()),
            new PagedResources.PageMetadata(size, number, total)
        );
        Page<Object> page = ConvertUtils.buildPageFromResources(resources, PageRequest.of(number, size));

        // verify converted value
        Assertions.assertEquals(
            size,
            page.getSize(),
            "ConvertUtils.buildPageFromResources() should populate page properties from input request."
        );
        Assertions.assertEquals(
            number,
            page.getNumber(),
            "ConvertUtils.buildPageFromResources() should populate page properties from input request."
        );
        Assertions.assertEquals(
            total,
            page.getTotalElements(),
            "ConvertUtils.buildPageFromResources() should populate page properties from loaded resources."
        );

        // verify content
        List<Object> content = page.getContent();
        Assertions.assertSame(
            object1,
            content.get(0),
            "ConvertUtils.buildPageFromResources() should populate page content from input request."
        );
        Assertions.assertSame(
            object2,
            content.get(1),
            "ConvertUtils.buildPageFromResources() should populate page content from input request."
        );
    }
}
