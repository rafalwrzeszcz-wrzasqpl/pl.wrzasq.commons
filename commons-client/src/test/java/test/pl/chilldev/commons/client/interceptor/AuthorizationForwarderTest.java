/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.client.interceptor;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import feign.RequestTemplate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import pl.chilldev.commons.client.interceptor.AuthorizationForwarder;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationForwarderTest
{
    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp()
    {
        Mockito.when(this.request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("foo");
    }

    @Test
    public void apply()
    {
        RequestTemplate template = new RequestTemplate();
        AuthorizationForwarder interceptor = new AuthorizationForwarder();
        interceptor.setRequest(this.request);

        interceptor.apply(template);

        Mockito.verify(this.request).getHeader(HttpHeaders.AUTHORIZATION);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        Assert.assertEquals(
            "AuthorizationForwarder.apply() should set Authorization HTTP header.",
            1, values.size()
        );
        Assert.assertTrue(
            "AuthorizationForwarder.apply() should set Authorization header based on the upstream request.",
            values.contains("foo")
        );
    }

    @Test
    public void applyWithHeader()
    {
        RequestTemplate template = new RequestTemplate();
        AuthorizationForwarder interceptor = new AuthorizationForwarder();
        interceptor.setRequest(this.request);

        template.header(HttpHeaders.AUTHORIZATION, "bar");

        interceptor.apply(template);

        Mockito.verify(this.request, Mockito.never()).getHeader(HttpHeaders.AUTHORIZATION);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        Assert.assertEquals(
            "AuthorizationForwarder.apply() should leave existing Authorization HTTP header.",
            1,
            values.size()
        );
        Assert.assertTrue(
            "AuthorizationForwarder.apply() should leave existing Authorization HTTP header.",
            values.contains("bar")
        );
    }
}
