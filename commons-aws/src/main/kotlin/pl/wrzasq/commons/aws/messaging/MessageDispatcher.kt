/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging

import java.io.IOException

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Generic handler for JSON-serialized messages.
 *
 * @param objectMapper JSON handler.
 * @param messageHandler Single message sender.
 * @param <ResponseType> Returned result type.
 */
open class MessageDispatcher<ResponseType>(
    private val objectMapper: ObjectMapper,
    private val messageHandler: (String) -> ResponseType
) {
    private val logger: Logger = LoggerFactory.getLogger(MessageDispatcher::class.java)

    /**
     * Handles message sending.
     *
     * @param message Message payload.
     * @return Operation result type.
     * @throws IllegalArgumentException When message could not be serialized.
     */
    fun send(message: Any): ResponseType = try {
        val payload = objectMapper.writeValueAsString(message)
        logger.info("Dispatching message {}.", payload)
        val result = messageHandler(payload)
        logger.debug("Message sent")
        result
    } catch (error: IOException) {
        logger.error("Failed to serialize message payload.", error)
        throw IllegalArgumentException("Could not serialize message payload.", error)
    }
}
