/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sns;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.sns.NotificationHandler;
import pl.chilldev.commons.aws.sns.TypedNotificationHandler;
import test.pl.chilldev.commons.aws.GenericMessage;

public class TypedNotificationHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Consumer<String> messageHandler;

    @Mock
    private Consumer<GenericMessage> genericMessageHandler;

    @Captor
    private ArgumentCaptor<GenericMessage> genericMessage;

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
            this.messageHandler,
            String.class
        );
        handler.process(event);

        Mockito.verify(this.messageHandler).accept(content);
    }

    @Test
    public void processGeneric()
    {
        ObjectMapper objectMapper = new ObjectMapper();

        UUID id0 = UUID.randomUUID();
        UUID id1 = UUID.randomUUID();

        String content = String.format(
            "{"
                + "\"ids\":["
                + "\"%s\","
                + "\"%s\""
                + "]}",
            id0.toString(),
            id1.toString()
        );

        SNSEvent.SNS message = new SNSEvent.SNS();
        message.setMessage(content);
        SNSEvent.SNSRecord record = new SNSEvent.SNSRecord();
        record.setSns(message);

        SNSEvent event = new SNSEvent();
        event.setRecords(Collections.singletonList(record));

        NotificationHandler handler = new TypedNotificationHandler(
            objectMapper,
            this.genericMessageHandler,
            GenericMessage.class
        );
        handler.process(event);

        Mockito.verify(this.genericMessageHandler).accept(this.genericMessage.capture());
        GenericMessage genericMessage = this.genericMessage.getValue();

        Assert.assertEquals(
            "TypedNotificationHandler.process() should deserialize typed message.",
            id0,
            genericMessage.getIds().get(0)
        );
        Assert.assertEquals(
            "TypedNotificationHandler.process() should deserialize typed message.",
            id1,
            genericMessage.getIds().get(1)
        );
    }
}
