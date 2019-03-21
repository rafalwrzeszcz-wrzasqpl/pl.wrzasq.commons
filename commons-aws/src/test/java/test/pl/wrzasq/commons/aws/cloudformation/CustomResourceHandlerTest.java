/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import com.amazonaws.services.lambda.runtime.Context;
import com.sunrun.cfnresponse.CfnRequest;
import com.sunrun.cfnresponse.CfnResponseSender;
import com.sunrun.cfnresponse.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.cloudformation.CustomResourceHandler;
import pl.wrzasq.commons.aws.cloudformation.CustomResourceResponse;

@ExtendWith(MockitoExtension.class)
public class CustomResourceHandlerTest
{
    @Mock
    private CfnResponseSender sender;

    @Mock
    private Context context;

    @Mock
    private BiFunction<Object, String, CustomResourceResponse<Object>> action;

    @Test
    public void handle() throws NoSuchFieldException, IllegalAccessException
    {
        CustomResourceHandler<Object, Object> handler = new CustomResourceHandler<>(
            this.action,
            this.action,
            this.action
        );
        this.setSender(handler);

        Object input = new Object();
        Object output = new Object();
        String id = "test";
        String physicalId = "arn:test";

        CfnRequest<Object> request = new CfnRequest<>();
        request.setRequestType("Create");
        request.setResourceProperties(input);
        request.setPhysicalResourceId(physicalId);

        Mockito.when(this.action.apply(input, physicalId)).thenReturn(new CustomResourceResponse<>(output, id));

        handler.handle(request, this.context);

        Mockito.verify(this.action).apply(input, physicalId);
        Mockito.verify(this.sender).send(request, Status.SUCCESS, this.context, "OK", output, id);
    }

    @Test
    public void handleError() throws NoSuchFieldException, IllegalAccessException
    {
        CustomResourceHandler<Object, Object> handler = new CustomResourceHandler<>(
            this.action,
            this.action,
            this.action
        );
        this.setSender(handler);

        Object input = new Object();
        String output = "Oh no!";
        String physicalId = "arn:test";

        CfnRequest<Object> request = new CfnRequest<>();
        request.setRequestType("Create");
        request.setResourceProperties(input);
        request.setPhysicalResourceId(physicalId);

        Mockito.when(this.action.apply(input, physicalId)).thenThrow(new RuntimeException(output));

        handler.handle(request, this.context);

        Mockito.verify(this.action).apply(input, physicalId);
        Mockito.verify(this.sender).send(request, Status.FAILED, this.context, output, null, physicalId);
    }

    private void setSender(CustomResourceHandler<?, ?> handler) throws NoSuchFieldException, IllegalAccessException
    {
        Field hack = handler.getClass().getDeclaredField("sender");
        hack.setAccessible(true);
        hack.set(handler, this.sender);
    }
}
