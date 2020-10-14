/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic handler for JSON-serialized messages.
 *
 * @param <ResponseType> Returned result type.
 */
public class MessageDispatcher<ResponseType> {
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    /**
     * JSON (de-)serialization handler.
     */
    private ObjectMapper objectMapper;

    /**
     * Message sender.
     */
    private Function<String, ResponseType> messageHandler;

    /**
     * Initializes message conversion handler.
     *
     * @param objectMapper JSON handler.
     * @param messageHandler Single message sender.
     */
    public MessageDispatcher(ObjectMapper objectMapper, Function<String, ResponseType> messageHandler) {
        this.objectMapper = objectMapper;
        this.messageHandler = messageHandler;
    }

    /**
     * Handles message sending.
     *
     * @param message Message payload.
     * @return Operation result type.
     * @throws IllegalArgumentException When message could not be serialized.
     */
    public ResponseType send(Object message) {
        try {
            var payload = this.objectMapper.writeValueAsString(message);

            this.logger.info("Dispatching message {}.", payload);
            var result =  this.messageHandler.apply(payload);
            this.logger.debug("Message sent");
            return result;
        } catch (IOException error) {
            this.logger.error("Failed to serialize message payload.", error);
            throw new IllegalArgumentException("Could not serialize message payload.", error);
        }
    }
}
