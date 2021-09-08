/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
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
 * @param messageHandler Single message consumer.
 * @param type Expected message content type.
 * @param <MessageType> Message type.
 */
class MessageHandler<MessageType>(
    private val objectMapper: ObjectMapper,
    private val messageHandler: (MessageType) -> Unit,
    private val type: Class<MessageType>
) {
    private val logger: Logger = LoggerFactory.getLogger(MessageHandler::class.java)

    /**
     * Handles message conversion.
     *
     * @param message Message content.
     */
    fun handle(message: String) {
        logger.info("Incoming message {}.", message)
        try {
            val data = objectMapper.readValue(message, type)
            messageHandler(data)
            logger.debug("Task processed.")
        } catch (error: IOException) {
            logger.error("Failed to parse event data.", error)
            throw IllegalArgumentException("Could not parse event data.", error)
        }
    }
}
