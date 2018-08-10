/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.chilldev.commons.aws.MessageHandler;

/**
 * SQS queue handler that processes typed message.
 */
public class TypedQueueHandler extends SimpleQueueHandler
{
    /**
     * Initializes SQS handler.
     *
     * @param sqs SQS client.
     * @param queueUrl SQS queue URL.
     * @param objectMapper JSON handler.
     * @param messageHandler Single message consumer.
     * @param type Message content type.
     * @param <Type> Message type.
     */
    public <Type> TypedQueueHandler(
        AmazonSQS sqs,
        String queueUrl,
        ObjectMapper objectMapper,
        Consumer<Type> messageHandler,
        Class<Type> type
    )
    {
        super(sqs, queueUrl, new MessageHandler<>(objectMapper, messageHandler, type)::handle);
    }

    /**
     * Initializes SQS handler.
     *
     * <p>
     *     This is a simplified version for AWS internal services, like AWS Lambda, which relies on environment
     *     permissions.
     * </p>
     *
     * @param queueUrl SQS queue URL.
     * @param objectMapper JSON handler.
     * @param messageHandler Single message consumer.
     * @param type Message content type.
     * @param <Type> Message type.
     */
    public <Type> TypedQueueHandler(
        String queueUrl,
        ObjectMapper objectMapper,
        Consumer<Type> messageHandler,
        Class<Type> type
    )
    {
        super(queueUrl, new MessageHandler<>(objectMapper, messageHandler, type)::handle);
    }
}
