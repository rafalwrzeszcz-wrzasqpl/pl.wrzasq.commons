/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.interceptor;

import java.util.ArrayList;
import java.util.Collection;

import feign.RequestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import pl.wrzasq.commons.client.interceptor.BearerTokenAuthorizer;

public class BearerTokenAuthorizerTest
{
    @Test
    public void apply()
    {
        String token = "test";
        RequestTemplate template = new RequestTemplate();

        new BearerTokenAuthorizer(token).apply(template);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        Assertions.assertEquals(
            "Bearer test",
            new ArrayList<>(values).get(0),
            "BearerTokenAuthorizer.apply() should set Authorization HTTP header."
        );
    }
}
