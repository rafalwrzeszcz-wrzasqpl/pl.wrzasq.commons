/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.codec;

import java.util.Collection;
import java.util.Map;

import feign.RequestTemplate;
import feign.codec.Encoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.wrzasq.commons.client.codec.SpringDataEncoder;

@ExtendWith(MockitoExtension.class)
public class SpringDataEncoderTest
{
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

        Assertions.assertTrue(
            queries.containsKey("page"),
            "SpringDataEncoder.encode() should populate page number parameter."
        );
        Assertions.assertTrue(
            queries.get("page").contains("2"),
            "SpringDataEncoder.encode() should set page parameter to page number from the request."
        );

        Assertions.assertTrue(
            queries.containsKey("size"),
            "SpringDataEncoder.encode() should populate page size parameter."
        );
        Assertions.assertTrue(
            queries.get("size").contains("3"),
            "SpringDataEncoder.encode() should set size parameter to page size from the request."
        );

        Assertions.assertTrue(
            queries.containsKey("sort"),
            "SpringDataEncoder.encode() should populate sorting parameter."
        );
        Assertions.assertTrue(
            queries.get("sort").contains("foo,ASC"),
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request."
        );
        Assertions.assertTrue(
            queries.get("sort").contains("bar,ASC"),
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request."
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

        Assertions.assertTrue(
            queries.containsKey("a"),
            "SpringDataEncoder.encode() should populate page number parameter under given name."
        );
        Assertions.assertTrue(
            queries.get("a").contains("2"),
            "SpringDataEncoder.encode() should set page parameter to page number from the request."
        );

        Assertions.assertTrue(
            queries.containsKey("b"),
            "SpringDataEncoder.encode() should populate page size parameter under given name."
        );
        Assertions.assertTrue(
            queries.get("b").contains("3"),
            "SpringDataEncoder.encode() should set size parameter to page size from the request."
        );

        Assertions.assertTrue(
            queries.containsKey("c"),
            "SpringDataEncoder.encode() should populate sorting parameter under given name."
        );
        Assertions.assertTrue(
            queries.get("c").contains("foo,ASC"),
            "SpringDataEncoder.encode() should set sort parameter to criteria from the request."
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
