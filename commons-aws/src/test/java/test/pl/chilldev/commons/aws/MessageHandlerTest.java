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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.chilldev.commons.aws.MessageHandler;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest
{
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

    @Test
    public void handleInvalidJson()
    {
        ObjectMapper objectMapper = new ObjectMapper();

        MessageHandler<Integer> messageHandler = new MessageHandler<>(
            objectMapper,
            this.messageHandler,
            Integer.TYPE
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> messageHandler.handle("test"),
            "MessageHandler.handle() should throw exception when passed data is of different type."
        );
    }
}
