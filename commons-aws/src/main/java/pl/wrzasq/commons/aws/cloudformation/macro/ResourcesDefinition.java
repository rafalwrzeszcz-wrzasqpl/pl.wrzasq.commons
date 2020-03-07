/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Generic structure of resource in section.
 */
@Data
public class ResourcesDefinition {
    /**
     * Resource type.
     */
    @JsonProperty("Type")
    private String type;

    /**
     * Condition handling.
     */
    @JsonProperty("Condition")
    private String condition;

    /**
     * Resource properties.
     */
    @JsonProperty("Properties")
    private Map<String, Object> properties;
}
