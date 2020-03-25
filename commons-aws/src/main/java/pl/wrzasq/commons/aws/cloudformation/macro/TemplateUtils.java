/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * CloudFormation template utilities.
 */
public class TemplateUtils {
    /**
     * Parameters section key.
     */
    public static final String SECTION_PARAMETERS = "Parameters";

    /**
     * Conditions section key.
     */
    public static final String SECTION_CONDITIONS = "Conditions";

    /**
     * Resources section key.
     */
    public static final String SECTION_RESOURCES = "Resources";

    /**
     * Property key "Type".
     */
    public static final String PROPERTY_KEY_TYPE = "Type";

    /**
     * Property key "DependsOn".
     */
    public static final String PROPERTY_KEY_DEPENDSON = "DependsOn";

    /**
     * Property key "Condition".
     */
    public static final String PROPERTY_KEY_CONDITION = "Condition";

    /**
     * Property key "Properties".
     */
    public static final String PROPERTY_KEY_PROPERTIES = "Properties";

    /**
     * Handles optional property by removing it from generic properties pool.
     *
     * @param properties Main properties container.
     * @param key Property key.
     * @param then Property action.
     * @param defaultValue Default property value.
     */
    public static void popProperty(
        Map<String, Object> properties,
        String key,
        Consumer<Object> then,
        Object defaultValue
    ) {
        if (properties.containsKey(key)) {
            then.accept(properties.get(key));
            properties.remove(key);
        } else if (defaultValue != null) {
            then.accept(defaultValue);
        }
    }

    /**
     * Creates resource definition structure.
     *
     * @param type CloudFormation resource type.
     * @param properties Resource configuration.
     * @param condition Creation condition.
     * @return Resource structure.
     */
    public static Map<String, Object> generateResource(
        String type,
        Map<String, Object> properties,
        String condition
    ) {
        var resource = new HashMap<String, Object>();
        resource.put(TemplateUtils.PROPERTY_KEY_TYPE, String.format("AWS::%s", type));

        if (!properties.isEmpty()) {
            resource.put(TemplateUtils.PROPERTY_KEY_PROPERTIES, properties);
        }

        if (condition != null && !condition.isEmpty()) {
            resource.put(TemplateUtils.PROPERTY_KEY_CONDITION, condition);
        }

        return resource;
    }

    /**
     * Returns !Ref reference call.
     *
     * @param reference Referred object ID.
     * @return !Ref call.
     */
    public static Map<String, Object> ref(String reference) {
        return Collections.singletonMap("Ref", reference);
    }

    /**
     * Returns !GetAtt reference call.
     *
     * @param resource Resource object ID.
     * @param attribute Attribute name.
     * @return !GetAtt call.
     */
    public static Map<String, Object> getAtt(String resource, String attribute) {
        return Collections.singletonMap("Fn::GetAtt", Arrays.asList(resource, attribute));
    }

    /**
     * Returns !Sub reference call.
     *
     * @param params Call parameters.
     * @return !Sub call.
     */
    public static Map<String, Object> sub(Object params) {
        return Collections.singletonMap("Fn::Sub", params);
    }

    /**
     * Safely converts value to a typed map.
     *
     * @param value Input object.
     * @return Typed map.
     */
    public static Map<String, Object> asMap(Object value) {
        var output = new HashMap<String, Object>();

        if (value instanceof Map) {
            for (var entry : ((Map<?, ?>) value).entrySet()) {
                output.put(entry.getKey().toString(), entry.getValue());
            }
        }

        return output;
    }
}
