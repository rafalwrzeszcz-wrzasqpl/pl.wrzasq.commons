/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.interceptor;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import feign.RequestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import pl.wrzasq.commons.client.interceptor.AuthorizationForwarder;

@ExtendWith(MockitoExtension.class)
public class AuthorizationForwarderTest {
    @Mock
    private HttpServletRequest request;

    @Test
    public void apply() {
        Mockito.when(this.request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("foo");

        RequestTemplate template = new RequestTemplate();
        AuthorizationForwarder interceptor = new AuthorizationForwarder();
        interceptor.setRequest(this.request);

        interceptor.apply(template);

        Mockito.verify(this.request).getHeader(HttpHeaders.AUTHORIZATION);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        Assertions.assertEquals(
            1, values.size(),
            "AuthorizationForwarder.apply() should set Authorization HTTP header."
        );
        Assertions.assertTrue(
            values.contains("foo"),
            "AuthorizationForwarder.apply() should set Authorization header based on the upstream request."
        );
    }

    @Test
    public void applyWithHeader() {
        RequestTemplate template = new RequestTemplate();
        AuthorizationForwarder interceptor = new AuthorizationForwarder();
        interceptor.setRequest(this.request);

        template.header(HttpHeaders.AUTHORIZATION, "bar");

        interceptor.apply(template);

        Mockito.verify(this.request, Mockito.never()).getHeader(HttpHeaders.AUTHORIZATION);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        Assertions.assertEquals(
            1,
            values.size(),
            "AuthorizationForwarder.apply() should leave existing Authorization HTTP header."
        );
        Assertions.assertTrue(
            values.contains("bar"),
            "AuthorizationForwarder.apply() should leave existing Authorization HTTP header."
        );
    }
}
