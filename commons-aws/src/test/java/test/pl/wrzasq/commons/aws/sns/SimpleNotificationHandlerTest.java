/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.sns;

import java.util.Arrays;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.sns.SimpleNotificationHandler;

@ExtendWith(MockitoExtension.class)
public class SimpleNotificationHandlerTest {
    @Mock
    private Consumer<String> messageHandler;

    @Test
    public void process() {
        var message1 = new SNSEvent.SNS();
        message1.setMessage("msg1");
        var record1 = new SNSEvent.SNSRecord();
        record1.setSns(message1);
        var message2 = new SNSEvent.SNS();
        message2.setMessage("msg2");
        var record2 = new SNSEvent.SNSRecord();
        record2.setSns(message2);

        var event = new SNSEvent();
        event.setRecords(Arrays.asList(record1, record2));

        var handler = new SimpleNotificationHandler(this.messageHandler);
        handler.process(event);

        Mockito.verify(this.messageHandler).accept(message1.getMessage());
        Mockito.verify(this.messageHandler).accept(message2.getMessage());
    }
}
