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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.chilldev.commons.aws.sqs.QueueHandler;

@ExtendWith(MockitoExtension.class)
public class QueueHandlerTest
{
    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<Message> messageHandler;

    @Test
    public void process() throws JsonProcessingException
    {
        // just for code coverage
        new QueueHandler(null, null);

        Message message1 = new Message().withReceiptHandle("msg1");
        Message message2 = new Message().withReceiptHandle("msg2");

        String queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message1, message2));

        QueueHandler handler = new QueueHandler(this.sqs, queue, this.messageHandler);
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message1.getReceiptHandle());
        Mockito.verify(this.sqs).deleteMessage(queue, message2.getReceiptHandle());
        Mockito.verify(this.messageHandler).accept(message1);
        Mockito.verify(this.messageHandler).accept(message2);
    }
}
