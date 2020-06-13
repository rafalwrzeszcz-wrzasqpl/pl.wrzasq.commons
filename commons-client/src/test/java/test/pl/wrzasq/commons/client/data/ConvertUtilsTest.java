/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.data;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedModel;
import pl.wrzasq.commons.client.data.ConvertUtils;

public class ConvertUtilsTest {
    @Test
    public void extractSort() {
        // just for code coverage
        new ConvertUtils();

        var extracted = ConvertUtils.extractSort(
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
        var object1 = new Object();
        var object2 = new Object();
        var size = 2;
        var number = 1;
        var total = 10;

        var resources = PagedModel.of(
            List.of(object1, object2),
            new PagedModel.PageMetadata(size, number, total)
        );
        var page = ConvertUtils.buildPageFromResources(resources, PageRequest.of(number, size));

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
        var content = page.getContent();
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
