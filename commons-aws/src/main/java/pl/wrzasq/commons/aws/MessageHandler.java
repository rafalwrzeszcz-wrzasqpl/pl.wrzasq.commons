/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws;

import java.io.IOException;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic handler for JSON-serialized messages.
 *
 * @param <Type> Message type.
 */
public class MessageHandler<Type> {
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    /**
     * JSON (de-)serialization handler.
     */
    private ObjectMapper objectMapper;

    /**
     * Message consumer.
     */
    private Consumer<Type> messageHandler;

    /**
     * Expected type.
     */
    private Class<Type> type;

    /**
     * Initializes message conversion handler.
     *
     * @param objectMapper JSON handler.
     * @param messageHandler Single message consumer.
     * @param type Expected message content type.
     */
    public MessageHandler(ObjectMapper objectMapper, Consumer<Type> messageHandler, Class<Type> type) {
        this.objectMapper = objectMapper;
        this.messageHandler = messageHandler;
        this.type = type;
    }

    /**
     * Handles message conversion.
     *
     * @param message Message content.
     */
    public void handle(String message) {
        this.logger.info("Incoming message {}.", message);

        try {
            Type data = this.objectMapper.readValue(message, this.type);
            this.messageHandler.accept(data);
            this.logger.debug("Task processed.");
        } catch (IOException error) {
            this.logger.error("Failed to parse event data.", error);
            throw new IllegalArgumentException("Could not parse event data.", error);
        }
    }
}
