/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging

import kotlinx.serialization.KSerializer
import java.io.IOException
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Generic handler for JSON-serialized messages.
 *
 * @param json JSON handler.
 * @param messageHandler Single message consumer.
 * @param typeSerializer Expected message type handler.
 * @param <MessageType> Message type.
 */
class MessageHandler<MessageType>(
    private val json: Json,
    private val messageHandler: (MessageType) -> Unit,
    private val typeSerializer: KSerializer<MessageType>
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
            val data = json.decodeFromString(typeSerializer, message)
            messageHandler(data)
            logger.debug("Task processed.")
        } catch (error: IOException) {
            logger.error("Failed to parse event data.", error)
            throw IllegalArgumentException("Could not parse event data.", error)
        }
    }
}
