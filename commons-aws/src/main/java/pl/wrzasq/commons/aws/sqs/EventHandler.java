/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.AllArgsConstructor;

/**
 * SQS incoming event handler.
 */
@AllArgsConstructor
public class EventHandler {
    /**
     * Message consumer.
     */
    private Consumer<SQSEvent.SQSMessage> messageHandler;

    /**
     * Processes event message.
     *
     * @param event SQS message event message.
     */
    public void process(SQSEvent event) {
        // SNS event for lambda always contain one record, but keep it uniform
        event.getRecords().forEach(this.messageHandler);
    }
}
