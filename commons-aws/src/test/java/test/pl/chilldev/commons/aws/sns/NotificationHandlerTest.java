/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sns;

import java.util.Arrays;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.sns.NotificationHandler;

public class NotificationHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Consumer<SNSEvent.SNS> messageHandler;

    @Test
    public void process()
    {
        SNSEvent.SNS message1 = new SNSEvent.SNS();
        message1.setMessage("msg1");
        SNSEvent.SNSRecord record1 = new SNSEvent.SNSRecord();
        record1.setSns(message1);
        SNSEvent.SNS message2 = new SNSEvent.SNS();
        message2.setMessage("msg2");
        SNSEvent.SNSRecord record2 = new SNSEvent.SNSRecord();
        record2.setSns(message2);

        SNSEvent event = new SNSEvent();
        event.setRecords(Arrays.asList(record1, record2));

        NotificationHandler handler = new NotificationHandler(this.messageHandler);
        handler.process(event);

        Mockito.verify(this.messageHandler).accept(message1);
        Mockito.verify(this.messageHandler).accept(message2);
    }
}
