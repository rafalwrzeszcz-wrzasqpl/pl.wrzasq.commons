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
import pl.chilldev.commons.jsonrpc.rpc.SpringDataModule;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

@RunWith(MockitoJUnitRunner.class)
public class SpringDataModuleTest
{
    interface TestService
        extends ContextInterface
    {
        @JsonRpcCall
        void test(
            @JsonRpcParam(optional = false, defaultValue = "5") Pageable pageable
        )
            throws JSONRPC2Error;

        @JsonRpcCall
        String optional(
            @JsonRpcParam(defaultValue = "4") Pageable pageable
        );

        @JsonRpcCall
        Page<String> mapping();
    }

    @Mock
    private SpringDataModuleTest.TestService context;

    @Test
    public void register()
    {
        Dispatcher<SpringDataModuleTest.TestService> dispatcher = this.buildDispatcher();

        Map<String, Object> params = new HashMap<>();
        params.put("limit", 4);

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
        Dispatcher<SpringDataModuleTest.TestService> dispatcher = this.buildDispatcher();

        String result = "bar";
        Map<String, Object> params = new HashMap<>();

        Mockito.when(
            this.context.optional(
                Matchers.isA(Pageable.class)
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
        Dispatcher<SpringDataModuleTest.TestService> dispatcher = this.buildDispatcher();

        List<String> data = new ArrayList<>();
        data.add("Grześ");
        data.add("Miłosz");

        Page<String> result = new PageImpl<>(data, new PageRequest(0, 2), 5);
        Map<String, Object> params = new HashMap<>();
        params.put("boolean",  false);

        Mockito.when(this.context.mapping()).thenReturn(result);

        JSONRPC2Request request = new JSONRPC2Request(
            "mapping",
            new HashMap<>(),
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

    private Dispatcher<SpringDataModuleTest.TestService> buildDispatcher()
    {
        Dispatcher<SpringDataModuleTest.TestService> dispatcher = new Dispatcher<>();
        Introspector introspector = new Introspector();
        SpringDataModule module = new SpringDataModule();

        module.initializeIntrospector(introspector);
        introspector.register(SpringDataModuleTest.TestService.class, dispatcher);

        return dispatcher;
    }
}
