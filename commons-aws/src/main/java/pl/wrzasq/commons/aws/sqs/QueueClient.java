/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.wrzasq.commons.aws.MessageDispatcher;
import pl.wrzasq.commons.json.ObjectMapperFactory;

/**
 * Queue-bound AWS SQS client.
 */
public class QueueClient extends MessageDispatcher<SendMessageResult> {
    /**
     * Initializes queue dispatcher.
     *
     * @param objectMapper JSON handler.
     * @param sqs SQS client.
     * @param queueUrl Target queue URL.
     */
    public QueueClient(ObjectMapper objectMapper, AmazonSQS sqs, String queueUrl) {
        super(objectMapper, (String payload) -> sqs.sendMessage(queueUrl, payload));
    }

    /**
     * Initializes client with default handlers.
     *
     * @param queueUrl SQS queue URL.
     */
    public QueueClient(String queueUrl) {
        this(ObjectMapperFactory.createObjectMapper(), AmazonSQSClientBuilder.standard().build(), queueUrl);
    }
}
