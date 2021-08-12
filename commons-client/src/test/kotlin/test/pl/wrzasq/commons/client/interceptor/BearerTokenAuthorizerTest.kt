/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.client.interceptor

import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.wrzasq.commons.client.interceptor.BearerTokenAuthorizer
import pl.wrzasq.commons.client.interceptor.HEADER_NAME_AUTHORIZATION

class BearerTokenAuthorizerTest {
    @Test
    fun apply() {
        val token = "test"
        val template = RequestTemplate()
        BearerTokenAuthorizer(token).apply(template)
        val values = template.headers()[HEADER_NAME_AUTHORIZATION]
        assertEquals("Bearer test", values?.first())
    }
}
