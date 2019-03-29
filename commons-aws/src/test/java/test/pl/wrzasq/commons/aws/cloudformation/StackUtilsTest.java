/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.cloudformation.StackUtils;

@ExtendWith(MockitoExtension.class)
public class StackUtilsTest
{
    @Mock
    private BiFunction<String, String, Object> converter;

    @Test
    public void buildSdkList()
    {
        String key1 = "foo";
        String key2 = "bar";
        String value1 = "baz";
        String value2 = "quux";
        Object object1 = new Object();
        Object object2 = new Object();

        Mockito
            .when(this.converter.apply(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(object1, object2);

        // just for code coverage
        new StackUtils();

        Map<String, String> input = new HashMap<>();
        input.put(key1, value1);
        input.put(key2, value2);

        Collection<Object> result = StackUtils.buildSdkList(input, this.converter);

        Mockito
            .verify(this.converter)
            .apply(key1, value1);
        Mockito
            .verify(this.converter)
            .apply(key2, value2);

        Assertions.assertEquals(
            2,
            result.size(),
            "StackUtils.buildSdkList() should return collection of all converted elements."
        );
        Assertions.assertTrue(
            result.contains(object1),
            "StackUtils.buildSdkList() should return collection of all converted elements."
        );
        Assertions.assertTrue(
            result.contains(object2),
            "StackUtils.buildSdkList() should return collection of all converted elements."
        );
    }

    @Test
    public void buildSdkListNull()
    {
        Assertions.assertEquals(
            0,
            StackUtils.buildSdkList(null, this.converter).size(),
            "StackUtils.buildSdkList() should return empty list in case of null source."
        );

        Mockito
            .verify(this.converter, Mockito.never())
            .apply(Mockito.anyString(), Mockito.anyString());
    }
}
