/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lambda handler that chains multiple sub-handlers to allow different inputs.
 *
 * <p>
 *     <strong>Note:</strong> It's usually bad idea to handle different cases by single Lambda function. It's purpose
 *     is to enable handling custom infrastructure-specific cases without involving real business logic (take pre-warm
 *     as an example).
 * </p>
 */
public class MultiHandler {
    /**
     * Single use-case handler.
     */
    @FunctionalInterface
    public interface CallHandler {
        /**
         * Attempts to handle a payload.
         *
         * @param input JSON input tree.
         * @param output Output stream.
         * @return State of the action handling (true indicates the action was handled and propagation should stop).
         */
        boolean handle(JsonNode input, OutputStream output);
    }

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(MultiHandler.class);

    /**
     * JSON handler.
     */
    @Getter
    private ObjectMapper objectMapper;

    /**
     * Registered handlers.
     */
    private List<MultiHandler.CallHandler> handlers = new ArrayList<>();

    /**
     * Initializes Lambda handler.
     *
     * @param objectMapper JSON de-serialization handler.
     */
    public MultiHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Adds sub-handler to current context.
     *
     * @param handler Payload handler.
     */
    public void registerHandler(MultiHandler.CallHandler handler) {
        this.handlers.add(handler);
    }

    /**
     * Handles invocation.
     *
     * @param inputStream Request input.
     * @param outputStream Output stream.
     * @throws IOException When JSON loading/dumping fails.
     */
    public void handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (outputStream) {
            var root = this.objectMapper.readTree(inputStream);

            // tries all handlers until any matches
            for (var handler : this.handlers) {
                this.logger.trace("Attempting {} to handle payload.", handlers.getClass().getName());
                if (handler.handle(root, outputStream)) {
                    this.logger.trace("Handled.");
                    return;
                } else {
                    this.logger.trace("Skipping.");
                }
            }

            this.logger.error("No handler was able to handle payload {}.", this.objectMapper.writeValueAsBytes(root));
        }
    }
}
