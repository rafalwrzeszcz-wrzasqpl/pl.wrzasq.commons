/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.lambda;

import java.io.OutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that reacts to pre-warm case.
 */
public class HeartbeatHandler implements MultiHandler.CallHandler {
    /**
     * Default field name for discriminator.
     */
    private static final String DEFAULT_DISCRIMINATOR_FIELD = "wrzasqpl:event:type";

    /**
     * Default field value for discriminator.
     */
    private static final String DEFAULT_DISCRIMINATOR_VALUE = "wrzasqpl:heartbeat";

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    /**
     * Event field name to rely on.
     */
    private String discriminatorField;

    /**
     * Event field value to rely on.
     */
    private String discriminatorValue;

    /**
     * Initializes handler for specific setup.
     *
     * @param discriminatorField Event field name.
     * @param discriminatorValue Expected field value.
     */
    public HeartbeatHandler(String discriminatorField, String discriminatorValue) {
        this.discriminatorField = discriminatorField;
        this.discriminatorValue = discriminatorValue;
    }

    /**
     * Initializes handler with default values.
     */
    public HeartbeatHandler() {
        this(HeartbeatHandler.DEFAULT_DISCRIMINATOR_FIELD, HeartbeatHandler.DEFAULT_DISCRIMINATOR_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handle(JsonNode input, OutputStream output) {
        if (input.has(this.discriminatorField)
            && input.get(this.discriminatorField).asText().equals(this.discriminatorValue)
        ) {
            this.logger.info("Heartbeat.");
            return true;
        } else {
            return false;
        }
    }
}
