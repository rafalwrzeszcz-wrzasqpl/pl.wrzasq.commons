/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.aws.lambda.HeartbeatHandler;

public class HeartbeatHandlerTest {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void handleNoDiscriminator() {
        Assertions.assertFalse(
            new HeartbeatHandler().handle(HeartbeatHandlerTest.objectMapper.createObjectNode(), null),
            "HeartbeatHandler.handle() should not handle request if there is no discriminator field present."
        );
    }

    @Test
    public void handleDifferentDiscriminator() {
        ObjectNode node = HeartbeatHandlerTest.objectMapper.createObjectNode();
        node.put("wrzasqpl:event:type", "test");

        Assertions.assertFalse(
            new HeartbeatHandler().handle(node, null),
            "HeartbeatHandler.handle() should not handle request if discriminator field has different value."
        );
    }

    @Test
    public void handle() {
        ObjectNode node = HeartbeatHandlerTest.objectMapper.createObjectNode();
        node.put("wrzasqpl:event:type", "wrzasqpl:heartbeat");

        Assertions.assertTrue(
            new HeartbeatHandler().handle(node, null),
            "HeartbeatHandler.handle() should handle heartbeat requests."
        );
    }

    @Test
    public void handleDifferentValues() {
        String fieldName = "eventType";
        String fieldValue = "pre-warm";

        ObjectNode node = HeartbeatHandlerTest.objectMapper.createObjectNode();
        node.put(fieldName, fieldValue);

        Assertions.assertTrue(
            new HeartbeatHandler(fieldName, fieldValue).handle(node, null),
            "HeartbeatHandler.handle() should handle heartbeat requests matching specified setup."
        );
    }
}
