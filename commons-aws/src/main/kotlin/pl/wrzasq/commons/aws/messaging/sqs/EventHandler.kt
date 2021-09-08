/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sqs

import com.amazonaws.services.lambda.runtime.events.SQSEvent

/**
 * SQS incoming event handler.
 *
 * @param messageHandler Message consumer.
 */
open class EventHandler(
    private val messageHandler: (SQSEvent.SQSMessage) -> Unit
) {
    /**
     * Processes event message.
     *
     * @param event SQS message event message.
     */
    fun process(event: SQSEvent) {
        event.records.forEach(messageHandler)
    }
}
