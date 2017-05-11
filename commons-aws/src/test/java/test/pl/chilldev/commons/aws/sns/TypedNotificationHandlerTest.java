/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sns;

import java.util.Collections;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.chilldev.commons.aws.sns.NotificationHandler;
import pl.chilldev.commons.aws.sns.TypedNotificationHandler;

@RunWith(MockitoJUnitRunner.class)
public class TypedNotificationHandlerTest
{
    @Mock
    private Consumer<String> messageHandler;

    @Test
    public void process() throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        String content = "test";

        SNSEvent.SNS message = new SNSEvent.SNS();
        message.setMessage(objectMapper.writeValueAsString(content));
        SNSEvent.SNSRecord record = new SNSEvent.SNSRecord();
        record.setSns(message);

        SNSEvent event = new SNSEvent();
        event.setRecords(Collections.singletonList(record));

        NotificationHandler handler = new TypedNotificationHandler(
            objectMapper,
            this.messageHandler
        );
        handler.process(event);

        Mockito.verify(this.messageHandler).accept(content);
    }
}
