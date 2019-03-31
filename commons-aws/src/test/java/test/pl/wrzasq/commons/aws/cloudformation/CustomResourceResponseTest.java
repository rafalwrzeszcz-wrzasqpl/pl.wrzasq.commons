/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.aws.cloudformation.CustomResourceResponse;

public class CustomResourceResponseTest {
    @Test
    public void nullPhysicalResourceId() {
        Object data = new Object();

        CustomResourceResponse<Object> response = new CustomResourceResponse<>(data);

        Assertions.assertSame(
            data,
            response.getData(),
            "CustomResourceResponse() constructor should keep custom resource data into response."
        );

        Assertions.assertNull(
            response.getPhysicalResourceId(),
            "CustomResourceResponse() single-argument constructor should set no physical resource ID."
        );
    }
}
