/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws;

import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.MessageHandler;

public class MessageHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Consumer<Integer> messageHandler;

    @Test
    public void handle() throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        MessageHandler<Integer> messageHandler = new MessageHandler<>(
            objectMapper,
            this.messageHandler,
            Integer.TYPE
        );

        Integer message = 44;
        messageHandler.handle(objectMapper.writeValueAsString(message));

        Mockito.verify(this.messageHandler).accept(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInvalidJson() throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        MessageHandler<Integer> messageHandler = new MessageHandler<>(
            objectMapper,
            this.messageHandler,
            Integer.TYPE
        );

        messageHandler.handle("test");
    }
}
