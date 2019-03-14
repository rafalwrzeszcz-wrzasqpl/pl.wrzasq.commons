/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.codec;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import pl.wrzasq.commons.client.codec.SpringErrorDecoder;

@ExtendWith(MockitoExtension.class)
public class SpringErrorDecoderTest
{
    @Mock
    private ErrorDecoder fallback;

    private Request request = Request.create(
        Request.HttpMethod.HEAD,
        "/",
        Collections.emptyMap(),
        new byte[] {},
        StandardCharsets.UTF_8
    );

    @Test
    public void decode4xx()
    {
        Response response = Response.builder()
            .status(404)
            .reason("not found")
            .headers(Collections.emptyMap())
            .request(this.request)
            .build();

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assertions.assertTrue(
            decoded instanceof HttpClientErrorException,
            "SpringErrorDecoder.decode() should produce Spring client exception."
        );
        Assertions.assertEquals(
            404,
            ((HttpStatusCodeException) decoded).getRawStatusCode(),
            "SpringErrorDecoder.decode() should preserve HTTP status code."
        );
        Assertions.assertEquals(
            "404 not found",
            decoded.getMessage(),
            "SpringErrorDecoder.decode() should preserve HTTP reason message."
        );

        Mockito.verifyZeroInteractions(this.fallback);
    }

    @Test
    public void decode5xx()
    {
        Response response = Response.builder()
            .status(502)
            .reason("server error")
            .headers(Collections.emptyMap())
            .request(this.request)
            .build();

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assertions.assertTrue(
            decoded instanceof HttpServerErrorException,
            "SpringErrorDecoder.decode() should produce Spring server exception."
        );
        Assertions.assertEquals(
            502,
            ((HttpStatusCodeException) decoded).getRawStatusCode(),
            "SpringErrorDecoder.decode() should preserve HTTP status code."
        );
        Assertions.assertEquals(
            "502 server error",
            decoded.getMessage(),
            "SpringErrorDecoder.decode() should preserve HTTP reason message."
        );

        Mockito.verifyZeroInteractions(this.fallback);
    }

    @Test
    public void decodeFallback()
    {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(Collections.emptyMap())
            .request(this.request)
            .build();
        Exception error = new Exception();

        Mockito.when(this.fallback.decode("test", response)).thenReturn(error);

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assertions.assertSame(
            error,
            decoded,
            "SpringErrorDecoder.decode() fall back when it's not a HTTP error."
        );

        Mockito.verify(this.fallback).decode("test", response);
    }
}
