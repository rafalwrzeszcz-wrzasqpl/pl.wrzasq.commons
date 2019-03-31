/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sns;

import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import lombok.AllArgsConstructor;

/**
 * SNS notifications handler.
 */
@AllArgsConstructor
public class NotificationHandler {
    /**
     * Message consumer.
     */
    private Consumer<SNSEvent.SNS> messageHandler;

    /**
     * Processes event notification.
     *
     * @param event Notification message.
     */
    public void process(SNSEvent event) {
        // SNS event for lambda always contain one record, but keep it uniform
        event.getRecords().stream()
            .map(SNSEvent.SNSRecord::getSNS)
            .forEach(this.messageHandler);
    }
}
