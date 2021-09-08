/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import pl.wrzasq.commons.aws.messaging.MessageDispatcher
import pl.wrzasq.commons.json.ObjectMapperFactory.createObjectMapper
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

/**
 * Queue-bound AWS SQS client.
 *
 * @param objectMapper JSON handler.
 * @param sqs SQS client.
 * @param queueUrl Target queue URL.
 */
class QueueClient(
    objectMapper: ObjectMapper = createObjectMapper(),
    sqs: SqsClient = SqsClient.create(),
    queueUrl: String
) : MessageDispatcher<SendMessageResponse>(
    objectMapper,
    { payload -> sqs.sendMessage { it.queueUrl(queueUrl).messageBody(payload) }}
)
