/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import java.io.IOException;

import java.net.InetSocketAddress;

import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.chilldev.commons.jsonrpc.mina.DispatcherIoHandler;
import pl.chilldev.commons.jsonrpc.mina.IoServiceUtils;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;

/**
 * Single listener worker.
 *
 * @param <ContextType> Execution context type.
 */
public class Listener<ContextType extends ContextInterface> extends Thread
    implements
        IoServiceUtils.Configuration
{
    /**
     * Sleep timeout.
     */
    public static final int SLEEP_TICK = 500;

    /**
     * Default packet size limit.
     */
    public static final int DEFAULT_PACKET_LIMIT = 33554432;

    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(Listener.class);

    /**
     * API context.
     */
    protected ContextType context;

    /**
     * JSON-RPC dispatcher.
     */
    protected Dispatcher<? super ContextType> dispatcher;

    /**
     * Execution interval.
     */
    protected int sleepTick = Listener.SLEEP_TICK;

    /**
     * Listening address.
     */
    protected InetSocketAddress address;

    /**
     * Maximum size of JSON-RPC packet.
     */
    protected int maxPacketSize = Listener.DEFAULT_PACKET_LIMIT;

    /**
     * Thread running flag.
     */
    protected boolean running = true;

    /**
     * Initializes listener thread.
     *
     * @param name Thread title.
     * @param context API context.
     * @param dispatcher JSON-RPC dispatcher.
     */
    public Listener(String name, ContextType context, Dispatcher<? super ContextType> dispatcher)
    {
        super(name);
        this.context = context;
        this.dispatcher = dispatcher;
    }

    /**
     * Sets sleep interval.
     *
     * @param sleepTick Sleep interval (in miliseconds).
     */
    public void setSleepTick(int sleepTick)
    {
        this.sleepTick = sleepTick;
    }

    /**
     * Sets listening interface.
     *
     * @param address Listening address.
     */
    public void setAddress(InetSocketAddress address)
    {
        this.address = address;
    }

    /**
     * Sets max JSON-RPC packet size.
     *
     * @param maxPacketSize Packet size in bytes.
     */
    public void setMaxPacketSize(int maxPacketSize)
    {
        this.maxPacketSize = maxPacketSize;
    }

    /**
     * Returns size of maximum JSON-RPC packet size.
     *
     * @return Maximum packet size.
     */
    @Override
    public int getMaxPacketSize()
    {
        return this.maxPacketSize;
    }

    /**
     * Sets shutdown flag.
     *
     * @return Self instance.
     */
    public Listener release()
    {
        this.running = false;

        return this;
    }

    /**
     * Perform the thread loop.
     */
    @Override
    public void run()
    {
        // check if there is any sense in running this listener
        if (this.address == null) {
            this.logger.info("\"{}\" was not enabled for connection, no point to start it.", this.getName());
            return;
        }

        NioSocketAcceptor acceptor = new NioSocketAcceptor();

        try {
            // network service configuration
            IoServiceUtils.initialize(
                acceptor,
                new DispatcherIoHandler<ContextType>(this.context, this.dispatcher),
                this
            );
            acceptor.setReuseAddress(true);
            acceptor.bind(this.address);

            this.logger.info("Started.");

            try {
                while (this.running) {
                    this.sleep(this.sleepTick);
                }
            } catch (InterruptedException error) {
                // don't worry - it's what we want in fact
            }
        } catch (IOException error) {
            this.logger.error("IO connection error: {}.", error.getMessage());
        } finally {
            // close connection
            acceptor.unbind();
            acceptor.dispose();
        }
    }
}
