/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client.interceptor

import feign.RequestInterceptor
import feign.RequestTemplate

/**
 * Authorization HTTP header name.
 */
const val HEADER_NAME_AUTHORIZATION = "Authorization"

private const val TYPE_BEARER = "Bearer "

/**
 * Feign request interceptor that injects custom HTTP authorization with Bearer token.
 *
 * @property token HTTP access token.
 */
class BearerTokenAuthorizer(
    private val token: String
) : RequestInterceptor {
    override fun apply(requestTemplate: RequestTemplate) {
        // to execute requests on behalf of the user set the same value as incoming request
        requestTemplate.header(
            HEADER_NAME_AUTHORIZATION,
            TYPE_BEARER + token
        )
    }
}
