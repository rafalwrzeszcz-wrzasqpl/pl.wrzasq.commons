/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * Default Jackson ObjectMapper provider.
 */
object ObjectMapperFactory {
    /**
     * Creates Jackson mapper.
     *
     * @return Object mapper to be used around the system.
     */
    fun createObjectMapper(): JsonMapper = JsonMapper.builder()
        .addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .build()
}
