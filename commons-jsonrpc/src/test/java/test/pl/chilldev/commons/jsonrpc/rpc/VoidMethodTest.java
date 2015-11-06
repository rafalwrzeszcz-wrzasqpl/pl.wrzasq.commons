/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.rpc.VoidMethod;

@RunWith(MockitoJUnitRunner.class)
public class VoidMethodTest
{
    @Mock
    private ContextInterface context;

    @Mock
    private VoidMethod<ContextInterface> method;

    @Test
    public void dispatch()
        throws
            JSONRPC2Error
    {
        VoidMethod.RequestHandler<ContextInterface> handler = new VoidMethod.RequestHandler<>(this.method);

        // create request
        JSONRPC2Request request = new JSONRPC2Request("version", "test");

        JSONRPC2Response response = handler.process(request, this.context);

        Assert.assertEquals(
            "Dispatcher should generate response with same ID that request has.",
            request.getID(),
            response.getID()
        );

        Mockito.verify(this.method).process(Matchers.same(request), Matchers.isA(ContextInterface.class));
    }
}
