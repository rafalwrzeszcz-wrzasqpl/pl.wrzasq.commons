/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client.codec;

import java.io.IOException;
import java.util.Collections;

import feign.Response;
import feign.codec.Decoder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import pl.chilldev.commons.client.codec.DelegateDecoder;

@RunWith(MockitoJUnitRunner.class)
public class DelegateDecoderTest
{
    @Mock
    private Decoder fallback;

    @Mock
    private Decoder typed;

    @Test
    public void decode() throws IOException
    {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.singletonMap(
                    HttpHeaders.CONTENT_TYPE,
                    Collections.singleton(MediaType.APPLICATION_JSON_VALUE)
                )
            )
            .build();
        Object result = this;

        Mockito.when(this.typed.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assert.assertSame(
            "DelegateDecoder.decode() should return result of decoder assigned to specified type.",
            result,
            decoder.decode(response, String.class)
        );

        Mockito.verifyZeroInteractions(this.fallback);
        Mockito.verify(this.typed).decode(response, String.class);
    }

    @Test
    public void decodeWithoutType() throws IOException
    {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.emptyMap()
            )
            .build();
        Object result = this;

        Mockito.when(this.fallback.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assert.assertSame(
            "DelegateDecoder.decode() should return fallback decoder result when no response type is available.",
            result,
            decoder.decode(response, String.class)
        );

        Mockito.verify(this.fallback).decode(response, String.class);
        Mockito.verifyZeroInteractions(this.typed);
    }

    @Test
    public void decodeFallback() throws IOException
    {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.singletonMap(
                    HttpHeaders.CONTENT_TYPE,
                    Collections.singleton(MediaType.TEXT_PLAIN_VALUE)
                )
            )
            .build();
        Object result = this;

        Mockito.when(this.fallback.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assert.assertSame(
            "DelegateDecoder.decode() should return fallback decoder result when no matching typed decoder found.",
            result,
            decoder.decode(response, String.class)
        );

        Mockito.verify(this.fallback).decode(response, String.class);
        Mockito.verifyZeroInteractions(this.typed);
    }
}
