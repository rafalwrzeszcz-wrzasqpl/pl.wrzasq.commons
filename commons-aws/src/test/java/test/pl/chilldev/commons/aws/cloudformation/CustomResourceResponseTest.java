/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.cloudformation;

import org.junit.Assert;
import org.junit.Test;
import pl.chilldev.commons.aws.cloudformation.CustomResourceResponse;

public class CustomResourceResponseTest
{
    @Test
    public void nullPhysicalResourceId()
    {
        Object data = new Object();

        CustomResourceResponse<Object> response = new CustomResourceResponse<>(data);

        Assert.assertSame(
            "CustomResourceResponse() constructor should keep custom resource data into response.",
            data,
            response.getData()
        );

        Assert.assertNull(
            "CustomResourceResponse() single-argument constructor should set no physical resource ID.",
            response.getPhysicalResourceId()
        );
    }
}
