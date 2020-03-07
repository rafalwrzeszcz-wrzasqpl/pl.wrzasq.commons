/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.Map;

/**
 * Container for template resources.
 */
public interface TemplateDefinition {
    /**
     * Template structure.
     *
     * @return Template structure.
     */
    Map<String, Object> getTemplate();
}
