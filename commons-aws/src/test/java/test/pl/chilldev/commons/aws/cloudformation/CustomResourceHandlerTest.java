/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.cloudformation;

import java.lang.reflect.Field;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;
import com.sunrun.cfnresponse.CfnRequest;
import com.sunrun.cfnresponse.CfnResponseSender;
import com.sunrun.cfnresponse.Status;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.cloudformation.CustomResourceHandler;

public class CustomResourceHandlerTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private CfnResponseSender sender;

    @Mock
    private Context context;

    @Mock
    private Function<Object, Object> action;

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

        CfnRequest<Object> request = new CfnRequest<>();
        request.setRequestType("Create");
        request.setResourceProperties(input);

        Mockito.when(this.action.apply(input)).thenReturn(output);

        handler.handle(request, this.context);

        Mockito.verify(this.action).apply(input);
        Mockito.verify(this.sender).send(request, Status.SUCCESS, this.context, "OK", output, null);
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

        CfnRequest<Object> request = new CfnRequest<>();
        request.setRequestType("Create");
        request.setResourceProperties(input);

        Mockito.when(this.action.apply(input)).thenThrow(new RuntimeException(output));

        handler.handle(request, this.context);

        Mockito.verify(this.action).apply(input);
        Mockito.verify(this.sender).send(request, Status.FAILED, this.context, output, null, null);
    }

    private void setSender(CustomResourceHandler<?, ?> handler) throws NoSuchFieldException, IllegalAccessException
    {
        Field hack = handler.getClass().getDeclaredField("sender");
        hack.setAccessible(true);
        hack.set(handler, this.sender);
    }
}
