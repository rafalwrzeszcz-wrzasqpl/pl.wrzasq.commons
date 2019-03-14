/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * Topic-wrapped AWS SNS client.
 */
@AllArgsConstructor
public class TopicClient
{
    /**
     * AWS SNS client.
     */
    private AmazonSNS sns;

    /**
     * JSON (de-)serialization handler.
     */
    private ObjectMapper objectMapper;

    /**
     * Topic ARN.
     */
    private String topicArn;

    /**
     * Initializes client wrapper with the default SNS client.
     *
     * <p>
     *     This is a simplified version for AWS internal services, like AWS Lambda, which relies on environment
     *     permissions.
     * </p>
     *
     * @param objectMapper JSON handler.
     * @param topicArn SNS topic ARN.
     */
    public TopicClient(ObjectMapper objectMapper, String topicArn)
    {
        this(AmazonSNSClientBuilder.standard().build(), objectMapper, topicArn);
    }

    /**
     * Publishes message to associated SNS topic.
     *
     * @param message Message to publish (will always be serialized to JSON, even if it's plain string).
     * @return Operation results.
     * @throws JsonProcessingException When message could not be serialized.
     */
    public PublishResult publish(Object message) throws JsonProcessingException
    {
        return this.sns.publish(this.topicArn, this.objectMapper.writeValueAsString(message));
    }
}
