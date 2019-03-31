/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.codec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import pl.wrzasq.commons.client.codec.DelegateDecoder;

@ExtendWith(MockitoExtension.class)
public class DelegateDecoderTest {
    @Mock
    private Decoder fallback;

    @Mock
    private Decoder typed;

    private Request request = Request.create(
        Request.HttpMethod.HEAD,
        "/",
        Collections.emptyMap(),
        new byte[] {},
        StandardCharsets.UTF_8
    );

    @Test
    public void decode() throws IOException {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.singletonMap(
                    HttpHeaders.CONTENT_TYPE,
                    Collections.singleton(MediaType.APPLICATION_JSON_VALUE)
                )
            )
            .request(this.request)
            .build();
        Object result = this;

        Mockito.when(this.typed.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assertions.assertSame(
            result,
            decoder.decode(response, String.class),
            "DelegateDecoder.decode() should return result of decoder assigned to specified type."
        );

        Mockito.verifyZeroInteractions(this.fallback);
        Mockito.verify(this.typed).decode(response, String.class);
    }

    @Test
    public void decodeWithExtraParameters() throws IOException {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.singletonMap(
                    HttpHeaders.CONTENT_TYPE,
                    Collections.singleton(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
            )
            .request(this.request)
            .build();
        Object result = this;

        Mockito.when(this.typed.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assertions.assertSame(
            result,
            decoder.decode(response, String.class),
            "DelegateDecoder.decode() should return result of decoder, ignoring extra type parameters."
        );

        Mockito.verifyZeroInteractions(this.fallback);
        Mockito.verify(this.typed).decode(response, String.class);
    }

    @Test
    public void decodeWithoutType() throws IOException {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.emptyMap()
            )
            .request(this.request)
            .build();
        Object result = this;

        Mockito.when(this.fallback.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assertions.assertSame(
            result,
            decoder.decode(response, String.class),
            "DelegateDecoder.decode() should return fallback decoder result when no response type is available."
        );

        Mockito.verify(this.fallback).decode(response, String.class);
        Mockito.verifyZeroInteractions(this.typed);
    }

    @Test
    public void decodeFallback() throws IOException {
        Response response = Response.builder()
            .status(200)
            .reason("ok")
            .headers(
                Collections.singletonMap(
                    HttpHeaders.CONTENT_TYPE,
                    Collections.singleton(MediaType.TEXT_PLAIN_VALUE)
                )
            )
            .request(this.request)
            .build();
        Object result = this;

        Mockito.when(this.fallback.decode(response, String.class)).thenReturn(result);

        DelegateDecoder decoder = new DelegateDecoder(this.fallback);
        decoder.registerTypeDecoder(MediaType.APPLICATION_JSON_VALUE, this.typed);

        Assertions.assertSame(
            result,
            decoder.decode(response, String.class),
            "DelegateDecoder.decode() should return fallback decoder result when no matching typed decoder found."
        );

        Mockito.verify(this.fallback).decode(response, String.class);
        Mockito.verifyZeroInteractions(this.typed);
    }
}
