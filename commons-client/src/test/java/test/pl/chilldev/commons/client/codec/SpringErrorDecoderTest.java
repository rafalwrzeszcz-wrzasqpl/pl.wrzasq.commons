/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client.codec;

import java.util.Collections;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import pl.chilldev.commons.client.codec.SpringErrorDecoder;

@RunWith(MockitoJUnitRunner.class)
public class SpringErrorDecoderTest
{
    @Mock
    private ErrorDecoder fallback;

    @Test
    public void decode4xx()
    {
        Response response = Response.builder()
            .status(404)
            .reason("not found")
            .headers(Collections.emptyMap())
            .build();

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assert.assertTrue(
            "SpringErrorDecoder.decode() should produce Spring client exception.",
            decoded instanceof HttpClientErrorException
        );
        Assert.assertEquals(
            "SpringErrorDecoder.decode() should preserve HTTP status code.",
            404,
            ((HttpStatusCodeException) decoded).getRawStatusCode()
        );
        Assert.assertEquals(
            "SpringErrorDecoder.decode() should preserve HTTP reason message.",
            "404 not found",
            decoded.getMessage()
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
            .build();

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assert.assertTrue(
            "SpringErrorDecoder.decode() should produce Spring server exception.",
            decoded instanceof HttpServerErrorException
        );
        Assert.assertEquals(
            "SpringErrorDecoder.decode() should preserve HTTP status code.",
            502,
            ((HttpStatusCodeException) decoded).getRawStatusCode()
        );
        Assert.assertEquals(
            "SpringErrorDecoder.decode() should preserve HTTP reason message.",
            "502 server error",
            decoded.getMessage()
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
            .build();
        Exception error = new Exception();

        Mockito.when(this.fallback.decode("test", response)).thenReturn(error);

        SpringErrorDecoder decoder = new SpringErrorDecoder(this.fallback);
        Exception decoded = decoder.decode("test", response);

        Assert.assertSame(
            "SpringErrorDecoder.decode() fall back when it's not a HTTP error.",
            error,
            decoded
        );

        Mockito.verify(this.fallback).decode("test", response);
    }
}
