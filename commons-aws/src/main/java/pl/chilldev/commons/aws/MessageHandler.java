/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws;

import java.io.IOException;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic handler for JSON-serialized messages.
 *
 * @param <Type> Message type.
 */
public class MessageHandler<Type> extends TypeReference<Type>
{
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
     * Initializes message conversion handler.
     *
     * @param objectMapper JSON handler.
     * @param messageHandler Single message consumer.
     */
    public MessageHandler(ObjectMapper objectMapper, Consumer<Type> messageHandler)
    {
        super();
        this.objectMapper = objectMapper;
        this.messageHandler = messageHandler;
    }

    /**
     * Handles message conversion.
     *
     * @param message Message content.
     */
    public void handle(String message)
    {
        this.logger.info("Incoming message {}.", message);

        try {
            Type data = this.objectMapper.readValue(message, this);
            this.messageHandler.accept(data);
            this.logger.debug("Task processed.");
        } catch (IOException error) {
            this.logger.error("Failed to parse event data.", error);
            throw new IllegalArgumentException("Could not parse event data.", error);
        }
    }
}
