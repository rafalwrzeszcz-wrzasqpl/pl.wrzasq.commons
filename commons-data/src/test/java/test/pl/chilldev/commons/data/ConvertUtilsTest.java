/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import pl.chilldev.commons.data.ConvertUtils;

public class ConvertUtilsTest
{
    @Test
    public void extractSort()
    {
        // just for code coverage
        new ConvertUtils();

        Collection<String> extracted = ConvertUtils.extractSort(
            Sort.by(
                new Sort.Order(Sort.Direction.DESC, "id"),
                new Sort.Order(Sort.Direction.ASC, "name")
            )
        );

        Assert.assertEquals(
            "ConvertUtils.extractSort() should return collection of all sort params.",
            2,
            extracted.size()
        );

        Assert.assertTrue(
            "ConvertUtils.extractSort() should return collection of all sort params.",
            extracted.contains("id,DESC")
        );
        Assert.assertTrue(
            "ConvertUtils.extractSort() should return collection of all sort params.",
            extracted.contains("name,ASC")
        );
    }

    @Test
    public void extractSortNull()
    {
        Assert.assertNull(
            "ConvertUtils.extractSort() should return NULL value for NULL input.",
            ConvertUtils.extractSort(null)
        );
    }

    @Test
    public void buildPageFromResources()
    {
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
        Assert.assertEquals(
            "ConvertUtils.buildPageFromResources() should populate page properties from input request.",
            size,
            page.getSize()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPageFromResources() should populate page properties from input request.",
            number,
            page.getNumber()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPageFromResources() should populate page properties from loaded resources.",
            total,
            page.getTotalElements()
        );

        // verify content
        List<Object> content = page.getContent();
        Assert.assertSame(
            "ConvertUtils.buildPageFromResources() should populate page content from input request.",
            object1,
            content.get(0)
        );
        Assert.assertSame(
            "ConvertUtils.buildPageFromResources() should populate page content from input request.",
            object2,
            content.get(1)
        );
    }
}
