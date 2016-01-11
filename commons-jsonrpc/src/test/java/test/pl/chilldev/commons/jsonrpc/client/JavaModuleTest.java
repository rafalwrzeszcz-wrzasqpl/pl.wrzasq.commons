/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.client;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.client.Connector;
import pl.chilldev.commons.jsonrpc.client.JavaModule;
import pl.chilldev.commons.jsonrpc.client.introspector.Introspector;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;

@RunWith(MockitoJUnitRunner.class)
public class JavaModuleTest
{
    public interface TestClient
    {
        @JsonRpcCall
        UUID id();
    }

    @Mock
    private Connector connector;

    @Test
    public void initializeIntrospector()
        throws
            IllegalAccessException,
            InstantiationException
    {
        UUID id = UUID.randomUUID();

        Introspector introspector = new Introspector();
        JavaModule module = new JavaModule();
        module.initializeIntrospector(introspector);

        JavaModuleTest.TestClient client = introspector.createClient(
            JavaModuleTest.TestClient.class,
            this.connector
        ).newInstance();

        Mockito.when(this.connector.execute(Matchers.eq("id"))).thenReturn(id.toString());

        UUID response = client.id();

        Assert.assertEquals(
            "Introspector.createClient() should cast response types.",
            id,
            response
        );
    }
}
