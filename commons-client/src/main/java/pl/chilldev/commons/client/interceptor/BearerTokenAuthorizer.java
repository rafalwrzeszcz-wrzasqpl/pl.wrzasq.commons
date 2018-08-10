/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.client.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;

/**
 * Feign request interceptor that injects custom HTTP authorization with Bearer token.
 */
@AllArgsConstructor
public class BearerTokenAuthorizer implements RequestInterceptor
{
    /**
     * Bearer token prefix.
     */
    private static final String TYPE_BEARER = "Bearer ";

    /**
     * HTTP access token..
     */
    private String token;

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        // set the same value as incoming request to execute requests on behalf of the user
        requestTemplate.header(HttpHeaders.AUTHORIZATION, BearerTokenAuthorizer.TYPE_BEARER + this.token);
    }
}
