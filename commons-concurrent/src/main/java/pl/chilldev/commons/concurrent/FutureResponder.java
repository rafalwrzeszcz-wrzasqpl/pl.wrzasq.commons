/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Generic future objects wrapper.
 *
 * @param <ResponseType> Any type that is expected to be return for your listener.
 */
public class FutureResponder<ResponseType> implements Callable<ResponseType>
{
    /**
     * Bound future.
     */
    protected FutureTask future;

    /**
     * Response to return.
     */
    protected ResponseType response;

    /**
     * Sets future to notify.
     *
     * @param future Target future.
     * @return Self instance.
     */
    public FutureResponder<ResponseType> setFuture(FutureTask future)
    {
        this.future = future;

        return this;
    }

    /**
     * Assigns response.
     *
     * @param response Response to return.
     * @return Self instance.
     */
    public FutureResponder<ResponseType> setResponse(ResponseType response)
    {
        this.response = response;
        // check if there is anything to run
        if (this.future != null) {
            this.future.run();
        }

        return this;
    }

    /**
     * Returns previously-sasigned response.
     *
     * @return Pre-defined response.
     */
    public ResponseType call()
    {
        return this.response;
    }
}
