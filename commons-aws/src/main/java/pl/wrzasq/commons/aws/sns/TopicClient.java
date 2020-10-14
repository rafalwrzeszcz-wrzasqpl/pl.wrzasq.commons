/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.wrzasq.commons.aws.MessageDispatcher;
import pl.wrzasq.commons.json.ObjectMapperFactory;

/**
 * Topic-wrapped AWS SNS client.
 */
public class TopicClient extends MessageDispatcher<PublishResult> {
    /**
     * Initializes queue dispatcher.
     *
     * @param objectMapper JSON handler.
     * @param sns SNS client.
     * @param topicArn Destination topic ARN.
     */
    public TopicClient(ObjectMapper objectMapper, AmazonSNS sns, String topicArn) {
        super(objectMapper, (String payload) -> sns.publish(topicArn, payload));
    }

    /**
     * Initializes client with default handlers.
     *
     * @param topicArn Destination topic ARN.
     */
    public TopicClient(String topicArn) {
        this(ObjectMapperFactory.createObjectMapper(), AmazonSNSClientBuilder.standard().build(), topicArn);
    }
}
