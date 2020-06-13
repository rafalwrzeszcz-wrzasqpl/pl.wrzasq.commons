/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 - 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.lambda.MultiHandler;

@ExtendWith(MockitoExtension.class)
public class MultiHandlerTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InputStream inputStream;

    @Mock
    private OutputStream outputStream;

    @Mock
    private JsonNode root;

    @Mock
    private MultiHandler.CallHandler handler1;

    @Mock
    private MultiHandler.CallHandler handler2;

    @Test
    public void handle() throws IOException {
        var handler = this.createMultiHandler();

        Mockito
            .when(this.objectMapper.readTree(this.inputStream))
            .thenReturn(this.root);

        Mockito
            .when(this.handler1.handle(this.root, this.outputStream))
            .thenReturn(true);

        handler.handle(this.inputStream, this.outputStream);

        Mockito.verify(this.handler1).handle(this.root, this.outputStream);
        Mockito.verifyNoMoreInteractions(this.handler2);
        Mockito.verify(this.objectMapper, Mockito.never()).writeValueAsBytes(this.root);
        Mockito.verify(this.outputStream).close();
    }

    @Test
    public void handleSecond() throws IOException {
        var handler = this.createMultiHandler();

        Mockito
            .when(this.objectMapper.readTree(this.inputStream))
            .thenReturn(this.root);

        Mockito
            .when(this.handler1.handle(this.root, this.outputStream))
            .thenReturn(false);

        Mockito
            .when(this.handler2.handle(this.root, this.outputStream))
            .thenReturn(true);

        handler.handle(this.inputStream, this.outputStream);

        Mockito.verify(this.handler1).handle(this.root, this.outputStream);
        Mockito.verify(this.handler2).handle(this.root, this.outputStream);
        Mockito.verify(this.objectMapper, Mockito.never()).writeValueAsBytes(this.root);
        Mockito.verify(this.outputStream).close();
    }

    @Test
    public void handleNoMatch() throws IOException {
        var handler = this.createMultiHandler();

        Mockito
            .when(this.objectMapper.readTree(this.inputStream))
            .thenReturn(this.root);

        Mockito
            .when(this.handler1.handle(this.root, this.outputStream))
            .thenReturn(false);

        Mockito
            .when(this.handler2.handle(this.root, this.outputStream))
            .thenReturn(false);

        handler.handle(this.inputStream, this.outputStream);

        Mockito.verify(this.handler1).handle(this.root, this.outputStream);
        Mockito.verify(this.handler2).handle(this.root, this.outputStream);
        Mockito.verify(this.objectMapper).writeValueAsBytes(this.root);
        Mockito.verify(this.outputStream).close();
    }

    @Test
    public void handleCloseOnError() throws IOException {
        var handler = this.createMultiHandler();

        Mockito
            .when(this.objectMapper.readTree(this.inputStream))
            .thenThrow(IOException.class);

        Assertions.assertThrows(
            IOException.class,
            () -> handler.handle(this.inputStream, this.outputStream),
            "MultiHandler.handle() should expose exception thrown by sub-handler."
        );

        Mockito.verifyNoMoreInteractions(this.handler1);
        Mockito.verifyNoMoreInteractions(this.handler2);
        Mockito.verify(this.objectMapper, Mockito.never()).writeValueAsBytes(this.root);
        Mockito.verify(this.outputStream).close();
    }

    private MultiHandler createMultiHandler() {
        var handler = new MultiHandler(this.objectMapper);
        handler.registerHandler(this.handler1);
        handler.registerHandler(this.handler2);
        return handler;
    }
}
