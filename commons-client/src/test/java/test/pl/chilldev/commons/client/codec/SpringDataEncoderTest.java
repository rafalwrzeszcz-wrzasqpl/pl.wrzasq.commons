/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client.codec;

import java.util.Collection;
import java.util.Map;

import feign.RequestTemplate;
import feign.codec.Encoder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.chilldev.commons.client.codec.SpringDataEncoder;

public class SpringDataEncoderTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Encoder fallback;

    @Test
    public void encode()
    {
        SpringDataEncoder encoder = new SpringDataEncoder(this.fallback);
        RequestTemplate template = new RequestTemplate();
        Pageable request = PageRequest.of(2, 3, Sort.Direction.ASC, "foo", "bar");

        encoder.encode(request, Pageable.class, template);

        Mockito.verifyZeroInteractions(this.fallback);

        Map<String, Collection<String>> queries = template.queries();

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate page number parameter.",
            queries.containsKey("page")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set page parameter to page number from the request.",
            queries.get("page").contains("2")
        );

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate page size parameter.",
            queries.containsKey("size")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set size parameter to page size from the request.",
            queries.get("size").contains("3")
        );

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate sorting parameter.",
            queries.containsKey("sort")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request.",
            queries.get("sort").contains("foo,ASC")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request.",
            queries.get("sort").contains("bar,ASC")
        );
    }

    @Test
    public void encodeCustomNames()
    {
        SpringDataEncoder encoder = new SpringDataEncoder(this.fallback, "a", "b", "c");
        RequestTemplate template = new RequestTemplate();
        Pageable request = PageRequest.of(2, 3, Sort.Direction.ASC, "foo");

        encoder.encode(request, Pageable.class, template);

        Mockito.verifyZeroInteractions(this.fallback);

        Map<String, Collection<String>> queries = template.queries();

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate page number parameter under given name.",
            queries.containsKey("a")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set page parameter to page number from the request.",
            queries.get("a").contains("2")
        );

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate page size parameter under given name.",
            queries.containsKey("b")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set size parameter to page size from the request.",
            queries.get("b").contains("3")
        );

        Assert.assertTrue(
            "SpringDataEncoder.encode() should populate sorting parameter under given name.",
            queries.containsKey("c")
        );
        Assert.assertTrue(
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request.",
            queries.get("c").contains("foo,ASC")
        );
    }

    @Test
    public void encodeFallback()
    {
        SpringDataEncoder encoder = new SpringDataEncoder(this.fallback);
        RequestTemplate template = new RequestTemplate();

        encoder.encode(this, this.getClass(), template);

        Mockito.verify(this.fallback).encode(this, this.getClass(), template);
    }
}
