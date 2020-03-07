/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.Map;
import java.util.function.Function;

import lombok.AllArgsConstructor;

/**
 * CloudFormation macro handler.
 */
@AllArgsConstructor
public class MacroHandler {
    /**
     * CloudFormation template provider.
     */
    private Function<Map<String, Object>, TemplateDefinition> templateFactory;

    /**
     * Handles template processing request.
     *
     * @param event CloudFormation macro event.
     * @return CloudFormation macro response.
     */
    public CloudFormationMacroResponse handleRequest(CloudFormationMacroRequest event) {
        return new CloudFormationMacroResponse(
            event.getRequestId(),
            CloudFormationMacroResponse.STATUS_SUCCESS,
            this.templateFactory.apply(event.getFragment()).getTemplate()
        );
    }
}
