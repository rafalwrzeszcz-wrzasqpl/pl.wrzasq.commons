/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 - 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sns

import com.fasterxml.jackson.databind.ObjectMapper
import pl.wrzasq.commons.aws.messaging.MessageDispatcher
import pl.wrzasq.commons.json.ObjectMapperFactory.createObjectMapper
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishResponse

/**
 * Topic-wrapped AWS SNS client.
 *
 * @param objectMapper JSON handler.
 * @param sns SNS client.
 * @param topicArn Destination topic ARN.
 */
class TopicClient(
    objectMapper: ObjectMapper = createObjectMapper(),
    sns: SnsClient = SnsClient.create(),
    topicArn: String
) : MessageDispatcher<PublishResponse>(
    objectMapper,
    { payload -> sns.publish { it.topicArn(topicArn).message(payload) }}
)
