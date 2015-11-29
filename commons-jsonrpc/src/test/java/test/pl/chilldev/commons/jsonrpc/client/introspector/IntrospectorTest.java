/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client.introspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.client.Connector;
import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

@RunWith(MockitoJUnitRunner.class)
public class IntrospectorTest
{
    public interface TestClient
    {
        @JsonRpcCall
        void test();

        @JsonRpcCall(name = "foo")
        void test(
            @JsonRpcParam int a
        );

        @JsonRpcCall
        UUID id(Pageable request);

        @JsonRpcCall
        List<String> test(
            @JsonRpcParam(name = "a") String query
        );

        void dummy();
    }

    @Mock
    private Connector connector;

    @Captor
    private ArgumentCaptor<Map<String, Object>> captor;

    @Test
    public void createClient()
        throws
            IllegalAccessException,
            InstantiationException
    {
        IntrospectorTest.TestClient client = Introspector.DEFAULT_INTROSPECTOR.createClient(
            IntrospectorTest.TestClient.class,
            this.connector
        ).newInstance();

        client.test();

        Mockito.verify(this.connector).execute("test");
    }

    @Test
    public void createClientNamed()
        throws
            IllegalAccessException,
            InstantiationException
    {
        IntrospectorTest.TestClient client = Introspector.DEFAULT_INTROSPECTOR.createClient(
            IntrospectorTest.TestClient.class,
            this.connector
        ).newInstance();

        client.test(12);

        Mockito.verify(this.connector).execute(Matchers.eq("foo"), this.captor.capture());
        Map<String, Object> params = this.captor.getValue();

        Assert.assertTrue(
            "Introspector.createClient() should build request parameters from method arguments.",
            params.containsKey("a")
        );
        Assert.assertEquals(
            "Introspector.createClient() should build request parameters from method arguments.",
            12,
            params.get("a")
        );
    }

    @Test
    public void createClientTypes()
        throws
            IllegalAccessException,
            InstantiationException
    {
        UUID id = UUID.randomUUID();
        Pageable request = new PageRequest(1, 2);

        IntrospectorTest.TestClient client = Introspector.DEFAULT_INTROSPECTOR.createClient(
            IntrospectorTest.TestClient.class,
            this.connector
        ).newInstance();

        Mockito.when(this.connector.execute(Matchers.eq("id"), this.captor.capture())).thenReturn(id.toString());

        UUID response = client.id(request);

        Assert.assertEquals(
            "Introspector.createClient() should cast response types.",
            id,
            response
        );

        Map<String, Object> params = this.captor.getValue();

        Assert.assertEquals(
            "Introspector.createClient() should handle argument of Pageable type.",
            1,
            params.get("page")
        );
        Assert.assertEquals(
            "Introspector.createClient() should handle argument of Pageable type.",
            2,
            params.get("limit")
        );
    }

    @Test
    public void createClientNamedParam()
        throws
            IllegalAccessException,
            InstantiationException
    {
        String query = "foo";
        String value = "bar";
        List<String> list = new ArrayList<>();
        list.add(value);

        IntrospectorTest.TestClient client = Introspector.DEFAULT_INTROSPECTOR.createClient(
            IntrospectorTest.TestClient.class,
            this.connector
        ).newInstance();

        Mockito.when(this.connector.execute(Matchers.eq("test"), this.captor.capture())).thenReturn(list);

        List<String> response = client.test(query);

        Map<String, Object> params = this.captor.getValue();

        Assert.assertTrue(
            "Introspector.createClient() should build request parameters from method arguments.",
            params.containsKey("a")
        );
        Assert.assertEquals(
            "Introspector.createClient() should build request parameters from method arguments.",
            query,
            params.get("a")
        );

        Assert.assertEquals(
            "Introspector.createClient() should forward response result.",
            1,
            response.size()
        );
        Assert.assertEquals(
            "Introspector.createClient() should forward response result.",
            value,
            response.get(0)
        );
    }
}
