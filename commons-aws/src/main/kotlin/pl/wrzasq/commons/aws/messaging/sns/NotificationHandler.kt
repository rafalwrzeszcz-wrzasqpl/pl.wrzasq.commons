/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sns

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord

/**
 * SNS notifications handler.
 *
 * @param messageHandler Message consumer.
 */
open class NotificationHandler(
    private val messageHandler: (SNS) -> Unit
) {
    /**
     * Processes event notification.
     *
     * @param event Notification message.
     */
    fun process(event: SNSEvent) {
        // SNS event for lambda always contain one record, but keep it uniform
        event.records.stream()
            .map(SNSRecord::getSNS)
            .forEach(messageHandler)
    }
}
