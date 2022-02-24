/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sqs

import kotlinx.serialization.json.Json
import pl.wrzasq.commons.aws.messaging.MessageDispatcher
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

/**
 * Queue-bound AWS SQS client.
 *
 * @param json JSON handler.
 * @param sqs SQS client.
 * @param queueUrl Target queue URL.
 */
class QueueClient(
    json: Json = Json.Default,
    sqs: SqsClient = SqsClient.create(),
    queueUrl: String
) : MessageDispatcher<SendMessageResponse>(
    json,
    { payload -> sqs.sendMessage { it.queueUrl(queueUrl).messageBody(payload) }}
)
