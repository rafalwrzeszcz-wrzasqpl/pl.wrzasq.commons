/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc.introspector;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

@RunWith(MockitoJUnitRunner.class)
public class IntrospectorTest
{
    interface TestService
        extends ContextInterface
    {
        @JsonRpcCall
        void test()
            throws JSONRPC2Error;

        @JsonRpcCall(name = "optional")
        String testOptionals();

        @JsonRpcCall
        void mapping(@JsonRpcParam(name = "boolean", optional = false) boolean bool);

        void nonRpc();
    }

    @Mock
    private IntrospectorTest.TestService context;

    @Test
    public void register()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();

        JSONRPC2Request request = new JSONRPC2Request(
            "test",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);
        Assert.assertNull(
            "Introspector.register() should create handler that returns no-result response for void method.",
            response.getResult()
        );
    }

    @Test
    public void registerOptionals()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.buildDispatcher();

        String result = "bar";
        Map<String, Object> params = new HashMap<>();

        Mockito.when(
            this.context.testOptionals()
        ).thenReturn(result);

        JSONRPC2Request request = new JSONRPC2Request(
            "optional",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);
        Assert.assertEquals(
            "Introspector.register() should create handler that returns method value as a response.",
            result,
            response.getResult()
        );
    }

    @Test
    public void registerMapping()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("boolean",  false);

        JSONRPC2Request request = new JSONRPC2Request(
            "mapping",
            params,
            1
        );

        dispatcher.dispatch(request, this.context);

        Mockito.verify(this.context).mapping(false);
    }

    @Test
    public void registerCallJSONRPC2Error()
        throws
            JSONRPC2Error
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();

        JSONRPC2Error error = new JSONRPC2Error(1, "test");
        Mockito.doThrow(error).when(this.context).test();

        JSONRPC2Request request = new JSONRPC2Request(
            "test",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);
        Assert.assertSame(
            "Introspector.register() should create handler that re-throws JSONRPC2Error instances directly.",
            error,
            response.getError()
        );
    }

    @Test
    public void registerCallOtherError()
        throws
            JSONRPC2Error
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();

        ClassCastException error = new ClassCastException();
        Mockito.doThrow(error).when(this.context).test();

        JSONRPC2Request request = new JSONRPC2Request(
            "test",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);
        Assert.assertTrue(
            "Introspector.register() should create wrapper that throws JSONRPC2Error wrapping other exceptions.",
            response.getError() instanceof JSONRPC2Error
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerInvalidService()
    {
        Introspector introspector = new Introspector();

        introspector.register(IntrospectorTest.TestService.class, new Dispatcher<>());
    }

    @Test
    public void createDispatcher()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = Introspector.createDefault().createDispatcher(
            IntrospectorTest.TestService.class
        );

        Map<String, Object> params = new HashMap<>();

        JSONRPC2Request request = new JSONRPC2Request(
            "test",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);
        Assert.assertNull(
            "Introspector.createDispatcher() should create dispatcher for provided facade class.",
            response.getResult()
        );
    }

    private Dispatcher<IntrospectorTest.TestService> buildDispatcher()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = new Dispatcher<>();

        Introspector introspector = new Introspector();

        // boolean retriever
        introspector.registerParameterProvider(
            boolean.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptBoolean(name, defaultValue.toLowerCase(Locale.ROOT).equals("true"))
                    : params.getBoolean(name);
            }
        );

        introspector.register(IntrospectorTest.TestService.class, dispatcher);

        return dispatcher;
    }
}
