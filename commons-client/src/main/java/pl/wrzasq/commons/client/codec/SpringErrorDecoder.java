/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client.codec;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Error decoder that, whenever possible, tries to construct meaningful Spring exception.
 */
public class SpringErrorDecoder implements ErrorDecoder {
    /**
     * Fallback error decoder.
     */
    private ErrorDecoder fallback;

    /**
     * Initializes object with fallback error decoder.
     *
     * @param fallback Fallback decoder.
     */
    public SpringErrorDecoder(ErrorDecoder fallback) {
        this.fallback = fallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        if (status.is5xxServerError()) {
            return new HttpServerErrorException(status, response.reason());
        } else if (status.is4xxClientError()) {
            return new HttpClientErrorException(status, response.reason());
        } else {
            return this.fallback.decode(methodKey, response);
        }
    }
}
