/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.concurrent;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import pl.chilldev.commons.concurrent.FutureResponder;

public class FutureResponderTest
{
    @Test
    public void setFuture()
    {
        FutureResponder<Object> responder = new FutureResponder<>();
        FutureTask<Object> future = new FutureTask<>(responder);
        assertSame(
            "FutureResponder.setFuture() should return reference to itself.",
            responder,
            responder.setFuture(future)
        );
    }

    @Test
    public void setResponse()
        throws
            InterruptedException,
            ExecutionException
    {
        String response = "Chillout";
        FutureResponder<String> responder = new FutureResponder<>();
        FutureTask<String> future = new FutureTask<>(responder);
        responder.setFuture(future);
        responder.setResponse(response);
        assertSame(
            "FutureResponder.setResponse() should mark future as executed.",
            response,
            future.get()
        );
    }

    @Test
    public void call()
    {
        String response = "Chillout";
        FutureResponder<String> responder = new FutureResponder<>();
        FutureTask<String> future = new FutureTask<>(responder);
        responder.setFuture(null);
        responder.setResponse(response);
        assertSame(
            "FutureResponder.call() should return previously calculated response.",
            response,
            responder.call()
        );
    }
}
