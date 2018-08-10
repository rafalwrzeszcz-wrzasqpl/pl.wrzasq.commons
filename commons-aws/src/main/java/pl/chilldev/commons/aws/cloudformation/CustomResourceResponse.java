/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws.cloudformation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Wrapper for custom resource output type.
 *
 * @param <OutputType> Structure of custom resource returner properties.
 */
@Data
@AllArgsConstructor
public class CustomResourceResponse<OutputType>
{
    /**
     * Response data.
     */
    private OutputType data;

    /**
     * Managed resource resource ID.
     */
    private String physicalResourceId;

    /**
     * Initializes new response to ephemeral resources (identified by current Lambda invocation log stream).
     *
     * @param data Response data.
     */
    public CustomResourceResponse(OutputType data)
    {
        this(data, null);
    }
}
