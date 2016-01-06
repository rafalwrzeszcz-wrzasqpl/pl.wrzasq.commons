/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.netty;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import io.netty.handler.codec.MessageToMessageEncoder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.netty.StringChannelInitializer;

@RunWith(MockitoJUnitRunner.class)
public class StringChannelInitializerTest
{
    @Mock
    private ChannelHandler handler;

    @Mock
    private Channel channel;

    @Mock
    private ChannelPipeline pipeline;

    @Mock
    private ChannelHandlerContext context;

    @Captor
    private ArgumentCaptor<ChannelHandler> captor;

    @Test
    public void initChannel()
    {
        StringChannelInitializer<Channel> initializer = new StringChannelInitializer<>(
            this.handler,
            () -> 12345
        );

        Mockito.when(this.channel.pipeline()).thenReturn(this.pipeline);

        initializer.initChannel(this.channel);

        Mockito.verify(this.pipeline).addLast(
            Matchers.isA(ChannelHandler.class),
            Matchers.isA(ChannelHandler.class),
            Matchers.isA(ChannelHandler.class),
            Matchers.isA(ChannelHandler.class),
            Matchers.isA(ChannelHandler.class),
            Matchers.same(this.handler)
        );
    }

    @Test
    public void encode()
        throws
            Exception
    {
        StringChannelInitializer<Channel> initializer = new StringChannelInitializer<>(
            this.handler,
            () -> 12345
        );

        Mockito.when(this.channel.pipeline()).thenReturn(this.pipeline);

        initializer.initChannel(this.channel);

        Mockito.verify(this.pipeline).addLast(this.captor.capture());

        ChannelHandler encoder = this.captor.getAllValues().get(3);

        Assert.assertTrue(
            "StringChannelInitializer should assign message converter.",
            encoder instanceof MessageToMessageEncoder
        );

        JSONRPC2Request request = new JSONRPC2Request("test", "0");

        ((MessageToMessageEncoder<?>) encoder).write(this.context, request, null);

        Mockito.verify(this.context).write(
            request.toJSONString() + "\n",
            null
        );
    }
}
