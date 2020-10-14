/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.sqs.QueueClient;

@ExtendWith(MockitoExtension.class)
public class QueueClientTest {
    @Mock
    private AmazonSQS sqs;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void send() throws JsonProcessingException {
        // just for code coverage
        new QueueClient(null);

        var queue = "https://test";
        var input = new Object();
        var message = "{}";
        var result = new SendMessageResult();

        var client = new QueueClient(this.objectMapper, this.sqs, queue);

        Mockito.when(this.objectMapper.writeValueAsString(input)).thenReturn(message);
        Mockito.when(this.sqs.sendMessage(queue, message)).thenReturn(result);

        Assertions.assertSame(
            result,
            client.send(input),
            "QueueClient.send() should send serialized message to configured queue."
        );
    }
}
