/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.rpc;

// JUnit includes
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

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

        Object expected = "foo";
        JSONRPC2Response response = handler.process(request, this.context);

        assertEquals(
            "Dispatcher should generate response with same ID that request has.",
            request.getID(),
            response.getID()
        );

        verify(this.method).process(same(request), isA(ContextInterface.class));
    }
}
