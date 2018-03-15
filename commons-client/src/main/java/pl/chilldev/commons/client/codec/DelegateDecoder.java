/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.client.codec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import org.springframework.http.HttpHeaders;

/**
 * Content-Type-sensitive decoder.
 */
public class DelegateDecoder implements Decoder
{
    /**
     * Fallback decoder for not-supported types.
     */
    private Decoder fallback;

    /**
     * Content-Type mapping for custom delegated decoders.
     */
    private Map<String, Decoder> delegates = new HashMap<>();

    /**
     * Initializes decoder.
     *
     * @param fallback Default fallback decoder.
     */
    public DelegateDecoder(Decoder fallback)
    {
        this.fallback = fallback;
    }

    /**
     * Registers handler for given MIME type.
     *
     * @param mimeType MIME content type.
     * @param decoder Type handler.
     * @return Self instance.
     */
    public DelegateDecoder registerTypeDecoder(String mimeType, Decoder decoder)
    {
        this.delegates.put(mimeType, decoder);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException
    {
        return this.delegates.getOrDefault(
            response.headers().getOrDefault(HttpHeaders.CONTENT_TYPE, Collections.emptyList())
                .stream()
                .findFirst()
                .orElse("")
                .split(";")[0],
            this.fallback
        )
            .decode(response, type);
    }
}
