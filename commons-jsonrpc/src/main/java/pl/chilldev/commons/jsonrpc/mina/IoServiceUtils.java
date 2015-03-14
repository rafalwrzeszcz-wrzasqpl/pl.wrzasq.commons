/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.mina;

import java.nio.charset.StandardCharsets;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;

/**
 * Utility class for Mina I/O services.
 */
public class IoServiceUtils
{
    /**
     * Interface for I/O services configuration.
     */
    public interface Configuration
    {
        /**
         * Returns maximum packet size.
         *
         * @return Maximum packet size.
         */
        int getMaxPacketSize();
    }

    /**
     * Key for codec chains.
     */
    protected static final String CHAIN_CODEC = "codec";

    /**
     * Initializes I/O service for text protocols.
     *
     * @param service I/O service connector.
     * @param handler Protocol handler.
     * @param config Configuration.
     */
    public static void initialize(IoService service, IoHandler handler, IoServiceUtils.Configuration config)
    {
        // text codec settings
        TextLineCodecFactory codec = new TextLineCodecFactory(StandardCharsets.UTF_8);
        codec.setDecoderMaxLineLength(config.getMaxPacketSize());
        codec.setEncoderMaxLineLength(config.getMaxPacketSize());

        // network service configuration
        service.getFilterChain().addLast(IoServiceUtils.CHAIN_CODEC, new ProtocolCodecFilter(codec));
        service.setHandler(handler);
    }
}
