/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.MessageDispatcher;

@ExtendWith(MockitoExtension.class)
public class MessageDispatcherTest {
    @Mock
    private Function<String, String> messageHandler;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void handle() throws JsonProcessingException {
        var expected = "test";

        var client = new MessageDispatcher<>(
            this.objectMapper,
            this.messageHandler
        );

        var message = 44;
        var serialized = "44";

        Mockito.when(this.objectMapper.writeValueAsString(message)).thenReturn(serialized);
        Mockito.when(this.messageHandler.apply(serialized)).thenReturn(expected);

        var response = client.send(message);

        Mockito.verify(this.messageHandler).apply(serialized);

        Assertions.assertEquals(
            expected,
            response,
            "MessageDispatcher.send() should return operation result."
        );
    }

    @Test
    public void handleInvalidJson() throws JsonProcessingException {
        var client = new MessageDispatcher<>(
            this.objectMapper,
            this.messageHandler
        );

        var message = 44;

        Mockito.when(this.objectMapper.writeValueAsString(message)).thenThrow(JsonProcessingException.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> client.send(message));
    }
}
