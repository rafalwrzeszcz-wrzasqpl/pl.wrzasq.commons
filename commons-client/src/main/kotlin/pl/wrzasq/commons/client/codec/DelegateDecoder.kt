/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.client.codec

import feign.Response
import feign.codec.Decoder
import java.lang.reflect.Type

/**
 * Content-Type HTTP header name.
 */
const val HEADER_NAME_CONTENT_TYPE = "Content-Type"

/**
 * Content-Type-sensitive decoder.
 *
 * @property fallback Default fallback decoder.
 */
class DelegateDecoder(
    private val fallback: Decoder
) : Decoder {
    private val delegates = mutableMapOf<String, Decoder>()

    /**
     * Registers handler for given MIME type.
     *
     * @param mimeType MIME content type.
     * @param decoder Type handler.
     * @return Self instance.
     */
    fun registerTypeDecoder(mimeType: String, decoder: Decoder): DelegateDecoder {
        delegates[mimeType] = decoder
        return this
    }

    override fun decode(response: Response, type: Type): Any = delegates.getOrDefault(
        (
            response.headers().getOrDefault(HEADER_NAME_CONTENT_TYPE, listOf()).firstOrNull() ?: ""
        )
            .split(";")[0],
        fallback
    )
        .decode(response, type)
}
