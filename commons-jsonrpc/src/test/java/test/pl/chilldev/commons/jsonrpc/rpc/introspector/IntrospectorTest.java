/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc.introspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
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
        void test(
            boolean bool,
            int integer,
            long longint,
            String string,
            UUID uuid,
            @JsonRpcParam(optional = false, defaultValue = "5") Pageable pageable,
            List<?> list
        )
            throws JSONRPC2Error;

        @JsonRpcCall(name = "optional")
        String testOptionals(
            @JsonRpcParam(defaultValue = "false") boolean bool,
            @JsonRpcParam(defaultValue = "123") int integer,
            @JsonRpcParam(defaultValue = "123456789") long longint,
            @JsonRpcParam(defaultValue = "foo") String string,
            @JsonRpcParam(defaultNull = true) UUID uuid,
            @JsonRpcParam(defaultValue = "4") Pageable pageable,
            @JsonRpcParam(defaultValue = "first") List<?> list
        );

        @JsonRpcCall
        Page<String> mapping(@JsonRpcParam(name = "boolean", optional = false) boolean bool);

        void nonRpc();
    }

    @Mock
    private IntrospectorTest.TestService context;

    @Test
    public void register()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.createDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("bool", true);
        params.put("integer", 123);
        params.put("longint", 123456789L);
        params.put("string", "test");
        params.put("uuid", UUID.randomUUID().toString());
        params.put("limit", 4);
        params.put("list", new ArrayList<String>());

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
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.createDispatcher();

        String result = "bar";
        Map<String, Object> params = new HashMap<>();

        Mockito.when(
            this.context.testOptionals(
                Matchers.eq(false),
                Matchers.eq(123),
                Matchers.eq(123456789L),
                Matchers.eq("foo"),
                (UUID) Matchers.isNull(),
                Matchers.isA(Pageable.class),
                Matchers.isA(List.class)
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

    @Test
    public void registerMapping()
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.createDispatcher();

        List<String> data = new ArrayList<>();
        data.add("Grześ");
        data.add("Miłosz");

        Page<String> result = new PageImpl<>(data, new PageRequest(0, 2), 5);
        Map<String, Object> params = new HashMap<>();
        params.put("boolean",  false);

        Mockito.when(this.context.mapping(false)).thenReturn(result);

        JSONRPC2Request request = new JSONRPC2Request(
            "mapping",
            params,
            1
        );

        JSONRPC2Response response = dispatcher.dispatch(request, this.context);

        Object output = response.getResult();
        Assert.assertTrue(
            "Introspector.register() should create handler that applies mapper for known result type.",
            output instanceof Map
        );

        Map<?, ?> map = (Map<?, ?>) output;
        Assert.assertTrue(
            "Page<?> response mapper should set total records count key.",
            map.containsKey("count")
        );
        Assert.assertTrue(
            "Page<?> response mapper should set current page data key.",
            map.containsKey("records")
        );

        Assert.assertEquals(
            "Page<?> response mapper should set total number of records.",
            5L,
            map.get("count")
        );

        Object content = map.get("records");
        Assert.assertTrue(
            "Page<?> response mapper should set result records.",
            content instanceof List
        );

        List<?> list = (List<?>) content;
        Assert.assertEquals(
            "Page<?> response mapper should put all result records into data list.",
            2,
            list.size()
        );
        Assert.assertTrue(
            "Page<?> response mapper should put all result records into data list.",
            list.contains("Grześ")
        );
        Assert.assertTrue(
            "Page<?> response mapper should put all result records into data list.",
            list.contains("Miłosz")
        );
    }

    @Test
    public void registerCallJSONRPC2Error()
        throws
            JSONRPC2Error
    {
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.createDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("bool", true);
        params.put("integer", 123);
        params.put("longint", 123456789L);
        params.put("string", "test");
        params.put("uuid", UUID.randomUUID().toString());
        params.put("limit", 4);
        params.put("list", new ArrayList<String>());

        JSONRPC2Error error = new JSONRPC2Error(1, "test");
        Mockito.doThrow(error).when(this.context).test(
            Matchers.eq(true),
            Matchers.eq(123),
            Matchers.eq(123456789L),
            Matchers.eq("test"),
            Matchers.isA(UUID.class),
            Matchers.isA(Pageable.class),
            Matchers.isA(List.class)
        );

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
        Dispatcher<IntrospectorTest.TestService> dispatcher = this.createDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("bool", true);
        params.put("integer", 123);
        params.put("longint", 123456789L);
        params.put("string", "test");
        params.put("uuid", UUID.randomUUID().toString());
        params.put("limit", 4);
        params.put("list", new ArrayList<String>());

        ClassCastException error = new ClassCastException();
        Mockito.doThrow(error).when(this.context).test(
            Matchers.eq(true),
            Matchers.eq(123),
            Matchers.eq(123456789L),
            Matchers.eq("test"),
            Matchers.isA(UUID.class),
            Matchers.isA(Pageable.class),
            Matchers.isA(List.class)
        );

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

        introspector.register(IntrospectorTest.TestService.class, null);
    }

    protected Dispatcher<IntrospectorTest.TestService> createDispatcher()
    {
        Introspector introspector = Introspector.createDefault();
        Dispatcher<IntrospectorTest.TestService> dispatcher = new Dispatcher<>();

        introspector.register(IntrospectorTest.TestService.class, dispatcher);

        return dispatcher;
    }
}
