/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

/**
 * SQS queue handler that simply processes message body.
 */
public class SimpleQueueHandler extends QueueHandler
{
    /**
     * Initializes handler.
     *
     * @param sqs SQS client.
     * @param queueUrl SQS queue URL.
     * @param messageBodyHandler Single message consumer.
     */
    public SimpleQueueHandler(AmazonSQS sqs, String queueUrl, Consumer<String> messageBodyHandler)
    {
        super(sqs, queueUrl, (Message message) -> messageBodyHandler.accept(message.getBody()));
    }

    /**
     * Initializes handler with the default SQS client.
     *
     * <p>
     *     This is a simplified version for AWS internal services, like AWS Lambda, which relies on environment
     *     permissions.
     * </p>
     *
     * @param queueUrl SQS queue URL.
     * @param messageBodyHandler Single message consumer.
     */
    public SimpleQueueHandler(String queueUrl, Consumer<String> messageBodyHandler)
    {
        super(queueUrl, (Message message) -> messageBodyHandler.accept(message.getBody()));
    }
}
