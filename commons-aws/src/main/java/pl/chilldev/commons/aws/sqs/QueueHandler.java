/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import lombok.AllArgsConstructor;

/**
 * SQS queue handler.
 */
@AllArgsConstructor
public class QueueHandler
{
    /**
     * AWS SQS client.
     */
    private AmazonSQS sqs;

    /**
     * SQS queue URL.
     */
    private String queueUrl;

    /**
     * Message consumer.
     */
    private Consumer<Message> messageHandler;

    /**
     * Initializes handler with the default SQS client.
     *
     * <p>
     *     This is a simplified version for AWS internal services, like AWS Lambda, which relies on environment
     *     permissions.
     * </p>
     *
     * @param queueUrl SQS queue URL.
     * @param messageHandler Single message consumer.
     */
    public QueueHandler(String queueUrl, Consumer<Message> messageHandler)
    {
        this(AmazonSQSClientBuilder.standard().build(), queueUrl, messageHandler);
    }

    /**
     * Processes queue messages.
     */
    public void process()
    {
        for (Message message : this.sqs.receiveMessage(this.queueUrl).getMessages()) {
            this.messageHandler.accept(message);
            this.sqs.deleteMessage(this.queueUrl, message.getReceiptHandle());
        }
    }
}
