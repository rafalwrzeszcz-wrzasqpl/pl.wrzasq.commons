/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sns

import kotlinx.serialization.json.Json
import pl.wrzasq.commons.aws.messaging.MessageDispatcher
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishResponse

/**
 * Topic-wrapped AWS SNS client.
 *
 * @param json JSON handler.
 * @param sns SNS client.
 * @param topicArn Destination topic ARN.
 */
class TopicClient(
    json: Json = Json.Default,
    sns: SnsClient = SnsClient.create(),
    topicArn: String
) : MessageDispatcher<PublishResponse>(
    json,
    { payload -> sns.publish { it.topicArn(topicArn).message(payload) }}
)
