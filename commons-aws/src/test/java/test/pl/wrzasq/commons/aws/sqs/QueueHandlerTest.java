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

@ExtendWith(MockitoExtension.class)
public class QueueHandlerTest {
    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<Message> messageHandler;

    @Test
    public void process() {
        // just for code coverage
        new QueueHandler(null, null);

        var message1 = new Message().withReceiptHandle("msg1");
        var message2 = new Message().withReceiptHandle("msg2");

        var queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message1, message2));

        var handler = new QueueHandler(this.sqs, queue, this.messageHandler);
        handler.process();

        Mockito.verify(this.sqs).receiveMessage(queue);
        Mockito.verify(this.sqs).deleteMessage(queue, message1.getReceiptHandle());
        Mockito.verify(this.sqs).deleteMessage(queue, message2.getReceiptHandle());
        Mockito.verify(this.messageHandler).accept(message1);
        Mockito.verify(this.messageHandler).accept(message2);
    }
}
