/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client;

import java.util.Map;

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
import pl.chilldev.commons.jsonrpc.client.SpringDataModule;
import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;

@RunWith(MockitoJUnitRunner.class)
public class SpringDataModuleTest
{
    public interface TestClient
    {
        @JsonRpcCall
        void id(Pageable request);
    }

    @Mock
    private Connector connector;

    @Captor
    private ArgumentCaptor<Map<String, Object>> captor;

    @Test
    public void initializeIntrospector()
        throws
            IllegalAccessException,
            InstantiationException
    {
        Pageable request = new PageRequest(1, 2);

        Introspector introspector = new Introspector();
        SpringDataModule module = new SpringDataModule();
        module.initializeIntrospector(introspector);

        SpringDataModuleTest.TestClient client = introspector.createClient(
            SpringDataModuleTest.TestClient.class,
            this.connector
        ).newInstance();

        client.id(request);

        Mockito.verify(this.connector).execute(Matchers.eq("id"), this.captor.capture());

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
}
