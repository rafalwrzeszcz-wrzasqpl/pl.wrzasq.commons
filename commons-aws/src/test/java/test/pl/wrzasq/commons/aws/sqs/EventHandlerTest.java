/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.sqs;

import java.util.List;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.sqs.EventHandler;

@ExtendWith(MockitoExtension.class)
public class EventHandlerTest {
    @Mock
    private Consumer<SQSEvent.SQSMessage> messageHandler;

    @Test
    public void process() {
        var message1 = new SQSEvent.SQSMessage();
        message1.setBody("msg1");
        var message2 = new SQSEvent.SQSMessage();
        message2.setBody("msg2");

        var event = new SQSEvent();
        event.setRecords(List.of(message1, message2));

        var handler = new EventHandler(this.messageHandler);
        handler.process(event);

        Mockito.verify(this.messageHandler).accept(message1);
        Mockito.verify(this.messageHandler).accept(message2);
    }
}
