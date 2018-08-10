/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.sqs.QueueHandler;
import pl.chilldev.commons.aws.sqs.SimpleQueueHandler;

public class SimpleQueueHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<String> messageHandler;

    @Test
    public void process() throws JsonProcessingException
    {
        // this is to make sure we resolve the AWS region for default region provider
        this.environmentVariables.set("AWS_REGION", "eu-central-1");

        // just for code coverage
        new SimpleQueueHandler(null, null);

        Message message1 = new Message().withBody("body1").withReceiptHandle("msg1");
        Message message2 = new Message().withBody("body2").withReceiptHandle("msg2");

        String queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message1, message2));

        QueueHandler handler = new SimpleQueueHandler(this.sqs, queue, this.messageHandler);
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message1.getReceiptHandle());
        Mockito.verify(this.sqs).deleteMessage(queue, message2.getReceiptHandle());
        Mockito.verify(this.messageHandler).accept(message1.getBody());
        Mockito.verify(this.messageHandler).accept(message2.getBody());
    }
}
