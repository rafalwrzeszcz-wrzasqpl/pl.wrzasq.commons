/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation.macro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.cloudformation.macro.TemplateUtils;

@ExtendWith(MockitoExtension.class)
public class TemplateUtilsTest {
    private static final String PROPERTY_KEY = "Foo";
    private static final String PROPERTY_VALUE = "Bar";

    @Mock
    private Consumer<Object> callback;

    @Test
    public void popProperty() {
        // just for code coverage
        new TemplateUtils();

        var properties = new HashMap<String, Object>();
        properties.put(TemplateUtilsTest.PROPERTY_KEY, TemplateUtilsTest.PROPERTY_VALUE);

        TemplateUtils.popProperty(properties, TemplateUtilsTest.PROPERTY_KEY, this.callback, null);

        Assertions.assertFalse(properties.containsKey(TemplateUtilsTest.PROPERTY_KEY));

        Mockito.verify(this.callback).accept(TemplateUtilsTest.PROPERTY_VALUE);
    }

    @Test
    public void popPropertyDefault() {
        var properties = new HashMap<String, Object>();

        TemplateUtils.popProperty(
            properties,
            TemplateUtilsTest.PROPERTY_KEY,
            this.callback,
            TemplateUtilsTest.PROPERTY_VALUE
        );

        Mockito.verify(this.callback).accept(TemplateUtilsTest.PROPERTY_VALUE);
    }

    @Test
    public void popPropertyUnexisting() {
        var properties = new HashMap<String, Object>();

        TemplateUtils.popProperty(properties, TemplateUtilsTest.PROPERTY_KEY, this.callback, null);

        Assertions.assertFalse(properties.containsKey(TemplateUtilsTest.PROPERTY_KEY));

        Mockito.verify(this.callback, Mockito.never()).accept(TemplateUtilsTest.PROPERTY_VALUE);
    }

    @Test
    public void generateResource() {
        var type = "Lambda::Function";
        var properties = new HashMap<String, Object>();
        properties.put(TemplateUtilsTest.PROPERTY_KEY, TemplateUtilsTest.PROPERTY_VALUE);
        var condition = "HasFunction";

        var resource = TemplateUtils.generateResource(type, properties, condition);

        Assertions.assertEquals("AWS::Lambda::Function", resource.get(TemplateUtils.PROPERTY_KEY_TYPE));
        Assertions.assertEquals(
            TemplateUtilsTest.PROPERTY_VALUE,
            TemplateUtils
                .asMap(resource.get(TemplateUtils.PROPERTY_KEY_PROPERTIES))
                .get(TemplateUtilsTest.PROPERTY_KEY)
        );
        Assertions.assertEquals(condition, resource.get(TemplateUtils.PROPERTY_KEY_CONDITION));
    }

    @Test
    public void generateResourceEmptyProperties() {
        var type = "Lambda::Function";
        var properties = new HashMap<String, Object>();
        var condition = "HasFunction";

        var resource = TemplateUtils.generateResource(type, properties, condition);

        Assertions.assertFalse(resource.containsKey(TemplateUtils.PROPERTY_KEY_PROPERTIES));
    }

    @Test
    public void generateResourceEmptyCondition() {
        var type = "Lambda::Function";
        var properties = new HashMap<String, Object>();
        properties.put(TemplateUtilsTest.PROPERTY_KEY, TemplateUtilsTest.PROPERTY_VALUE);
        var condition = "";

        var resource = TemplateUtils.generateResource(type, properties, condition);

        Assertions.assertFalse(resource.containsKey(TemplateUtils.PROPERTY_KEY_CONDITION));
    }

    @Test
    public void generateResourceNoCondition() {
        var type = "Lambda::Function";
        var properties = new HashMap<String, Object>();
        properties.put(TemplateUtilsTest.PROPERTY_KEY, TemplateUtilsTest.PROPERTY_VALUE);

        var resource = TemplateUtils.generateResource(type, properties, null);

        Assertions.assertFalse(resource.containsKey(TemplateUtils.PROPERTY_KEY_CONDITION));
    }

    @Test
    public void ref() {
        var reference = "Foo";
        var output = TemplateUtils.ref(reference);

        Assertions.assertEquals(1, output.size());
        Assertions.assertTrue(output.containsKey("Ref"));
        Assertions.assertEquals(reference, output.get("Ref"));
    }

    @Test
    public void getAtt() {
        var reference = "Foo";
        var attribute = "Outputs.SubReturn";
        var output = TemplateUtils.getAtt(reference, attribute);

        Assertions.assertEquals(1, output.size());
        Assertions.assertTrue(output.containsKey("Fn::GetAtt"));
        Assertions.assertEquals(Arrays.asList(reference, attribute), output.get("Fn::GetAtt"));
    }

    @Test
    public void sub() {
        var params = "Foo:${Bar}";
        var output = TemplateUtils.sub(params);

        Assertions.assertEquals(1, output.size());
        Assertions.assertTrue(output.containsKey("Fn::Sub"));
        Assertions.assertEquals(params, output.get("Fn::Sub"));
    }

    @Test
    public void asMap() {
        var input = new HashMap<>();
        input.put(TemplateUtilsTest.PROPERTY_KEY, TemplateUtilsTest.PROPERTY_VALUE);
        input.put("Baz", 123);

        var output = TemplateUtils.asMap(input);

        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals(TemplateUtilsTest.PROPERTY_VALUE, output.get(TemplateUtilsTest.PROPERTY_KEY));
        Assertions.assertEquals(123, output.get("Baz"));
    }

    @Test
    public void asMapNotMap() {
        var input = "Foo";

        var output = TemplateUtils.asMap(input);

        Assertions.assertTrue(output.isEmpty());
    }
}
