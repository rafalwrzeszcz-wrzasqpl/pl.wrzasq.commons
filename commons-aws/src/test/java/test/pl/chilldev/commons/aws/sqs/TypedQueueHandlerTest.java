/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sqs;

import java.util.UUID;
import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.sqs.QueueHandler;
import pl.chilldev.commons.aws.sqs.TypedQueueHandler;
import test.pl.chilldev.commons.aws.GenericMessage;

public class TypedQueueHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<String> messageHandler;

    @Mock
    private Consumer<GenericMessage> genericMessageHandler;

    @Captor
    private ArgumentCaptor<GenericMessage> genericMessage;

    @Test
    public void process() throws JsonProcessingException
    {
        // this is to make sure we resolve the AWS region for default region provider
        this.environmentVariables.set("AWS_REGION", "eu-central-1");

        // just for code coverage
        new TypedQueueHandler(null, null, null, Object.class);

        ObjectMapper objectMapper = new ObjectMapper();

        String content = "test";

        Message message = new Message().withBody(objectMapper.writeValueAsString(content)).withReceiptHandle("msg");

        String queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message));

        QueueHandler handler = new TypedQueueHandler(
            this.sqs,
            queue,
            objectMapper,
            this.messageHandler,
            String.class
        );
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message.getReceiptHandle());
        Mockito.verify(this.messageHandler).accept(content);
    }

    @Test
    public void processGeneric()
    {
        ObjectMapper objectMapper = new ObjectMapper();

        UUID id0 = UUID.randomUUID();
        UUID id1 = UUID.randomUUID();

        String content = String.format(
            "{"
                + "\"ids\":["
                + "\"%s\","
                + "\"%s\""
                + "]}",
            id0.toString(),
            id1.toString()
        );

        Message message = new Message().withBody(content).withReceiptHandle("msg");

        String queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message));

        QueueHandler handler = new TypedQueueHandler(
            this.sqs,
            queue,
            objectMapper,
            this.genericMessageHandler,
            GenericMessage.class
        );
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message.getReceiptHandle());
        Mockito.verify(this.genericMessageHandler).accept(this.genericMessage.capture());

        GenericMessage genericMessage = this.genericMessage.getValue();

        Assert.assertEquals(
            "TypedQueueHandler.process() should deserialize typed message.",
            id0,
            genericMessage.getIds().get(0)
        );
        Assert.assertEquals(
            "TypedQueueHandler.process() should deserialize typed message.",
            id1,
            genericMessage.getIds().get(1)
        );
    }
}
