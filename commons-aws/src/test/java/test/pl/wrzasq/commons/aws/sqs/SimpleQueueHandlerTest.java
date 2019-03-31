/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.sqs;

import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.sqs.QueueHandler;
import pl.wrzasq.commons.aws.sqs.SimpleQueueHandler;

@ExtendWith(MockitoExtension.class)
public class SimpleQueueHandlerTest {
    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<String> messageHandler;

    @Test
    public void process() {
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
