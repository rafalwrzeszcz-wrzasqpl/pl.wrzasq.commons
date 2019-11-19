/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.sqs;

import java.util.UUID;
import java.util.function.Consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.sqs.TypedQueueHandler;
import test.pl.wrzasq.commons.aws.GenericMessage;

@ExtendWith(MockitoExtension.class)
public class TypedQueueHandlerTest {
    @Mock
    private AmazonSQS sqs;

    @Mock
    private Consumer<String> messageHandler;

    @Mock
    private Consumer<GenericMessage> genericMessageHandler;

    @Captor
    private ArgumentCaptor<GenericMessage> genericMessage;

    @Test
    public void process() throws JsonProcessingException {
        // just for code coverage
        new TypedQueueHandler(null, null, null, Object.class);

        var objectMapper = new ObjectMapper();
        var content = "test";
        var message = new Message().withBody(objectMapper.writeValueAsString(content)).withReceiptHandle("msg");
        var queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message));

        var handler = new TypedQueueHandler(
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
    public void processGeneric() {
        var objectMapper = new ObjectMapper();

        var id0 = UUID.randomUUID();
        var id1 = UUID.randomUUID();

        var content = String.format(
            "{"
                + "\"ids\":["
                + "\"%s\","
                + "\"%s\""
                + "]}",
            id0.toString(),
            id1.toString()
        );

        var message = new Message().withBody(content).withReceiptHandle("msg");
        var queue = "http://test";

        Mockito.when(this.sqs.receiveMessage(queue))
            .thenReturn(new ReceiveMessageResult().withMessages(message));

        var handler = new TypedQueueHandler(
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

        var genericMessage = this.genericMessage.getValue();

        Assertions.assertEquals(
            id0,
            genericMessage.getIds().get(0),
            "TypedQueueHandler.process() should deserialize typed message."
        );
        Assertions.assertEquals(
            id1,
            genericMessage.getIds().get(1),
            "TypedQueueHandler.process() should deserialize typed message."
        );
    }
}
