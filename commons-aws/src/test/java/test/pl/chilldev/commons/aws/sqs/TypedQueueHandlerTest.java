/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.chilldev.commons.aws.sqs.QueueHandler;
import pl.chilldev.commons.aws.sqs.TypedQueueHandler;

@RunWith(MockitoJUnitRunner.class)
public class TypedQueueHandlerTest
{
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
        new TypedQueueHandler(null, null, null);

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
            this.messageHandler
        );
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message.getReceiptHandle());
        Mockito.verify(this.messageHandler).accept(content);
    }
}
