/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;
import pl.chilldev.commons.jsonrpc.rpc.JavaModule;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

@RunWith(MockitoJUnitRunner.class)
public class JavaModuleTest
{
    interface TestService
        extends ContextInterface
    {
        @JsonRpcCall
        void test(
            boolean bool,
            int integer,
            long longint,
            String string,
            UUID uuid,
            List<?> list,
            Set<?> set
        )
            throws JSONRPC2Error;

        @JsonRpcCall
        String optional(
            @JsonRpcParam(defaultValue = "false") boolean bool,
            @JsonRpcParam(defaultValue = "123") int integer,
            @JsonRpcParam(defaultValue = "123456789") long longint,
            @JsonRpcParam(defaultValue = "foo") String string,
            @JsonRpcParam(defaultNull = true) UUID uuid,
            @JsonRpcParam(defaultValue = "first") List<?> list,
            @JsonRpcParam(defaultValue = "single") Set<?> set
        );
    }

    @Mock
    private JavaModuleTest.TestService context;

    @Test
    public void register()
    {
        Dispatcher<JavaModuleTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("bool", true);
        params.put("integer", 123);
        params.put("longint", 123456789L);
        params.put("string", "test");
        params.put("uuid", UUID.randomUUID().toString());
        params.put("list", new ArrayList<String>());
        params.put("set", new ArrayList<String>());

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
        Dispatcher<JavaModuleTest.TestService> dispatcher = this.buildDispatcher();

        String result = "bar";
        Map<String, Object> params = new HashMap<>();

        Mockito.when(
            this.context.optional(
                Matchers.eq(false),
                Matchers.eq(123),
                Matchers.eq(123456789L),
                Matchers.eq("foo"),
                (UUID) Matchers.isNull(),
                Matchers.isA(List.class),
                Matchers.isA(Set.class)
            )
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

    private Dispatcher<JavaModuleTest.TestService> buildDispatcher()
    {
        Dispatcher<JavaModuleTest.TestService> dispatcher = new Dispatcher<>();
        Introspector introspector = new Introspector();
        JavaModule module = new JavaModule();

        module.initializeIntrospector(introspector);
        introspector.register(JavaModuleTest.TestService.class, dispatcher);

        return dispatcher;
    }
}
