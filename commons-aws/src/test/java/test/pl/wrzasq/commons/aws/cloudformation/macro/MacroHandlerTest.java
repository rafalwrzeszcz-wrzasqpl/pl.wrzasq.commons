/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.cloudformation.macro.CloudFormationMacroRequest;
import pl.wrzasq.commons.aws.cloudformation.macro.CloudFormationMacroResponse;
import pl.wrzasq.commons.aws.cloudformation.macro.MacroHandler;
import pl.wrzasq.commons.aws.cloudformation.macro.TemplateDefinition;

@ExtendWith(MockitoExtension.class)
public class MacroHandlerTest {
    @Mock
    private TemplateDefinition definition;

    @Mock
    private Function<Map<String, Object>, TemplateDefinition> factory;

    @Test
    public void handleRequest() {
        var fragment = new HashMap<String, Object>();
        var template = new HashMap<String, Object>();
        var requestId = "123";

        Mockito
            .when(this.factory.apply(fragment))
            .thenReturn(this.definition);
        Mockito
            .when(this.definition.getTemplate())
            .thenReturn(template);

        var handler = new MacroHandler(this.factory);

        var response = handler.handleRequest(new CloudFormationMacroRequest(requestId, fragment));

        Assertions.assertEquals(requestId, response.getRequestId());
        Assertions.assertEquals(CloudFormationMacroResponse.STATUS_SUCCESS, response.getStatus());
        Assertions.assertEquals(template, response.getFragment());
    }
}
