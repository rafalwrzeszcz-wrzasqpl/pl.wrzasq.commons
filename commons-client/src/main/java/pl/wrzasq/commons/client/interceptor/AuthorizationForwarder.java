/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client.interceptor;

import javax.servlet.http.HttpServletRequest;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Feign request interceptor that forwards HTTP Authorization header downstream.
 */
@Service
@RequestScope
public class AuthorizationForwarder implements RequestInterceptor
{
    /**
     * Current HTTP upstream request.
     */
    @Autowired
    @Setter
    private HttpServletRequest request;

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        // do not override header if already explicitly specified
        if (!requestTemplate.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            // set the same value as incoming request to execute requests on behalf of the user
            requestTemplate.header(HttpHeaders.AUTHORIZATION, this.request.getHeader(HttpHeaders.AUTHORIZATION));
        }
    }
}
