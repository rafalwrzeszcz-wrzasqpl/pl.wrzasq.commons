/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws.sns;

import java.util.function.Consumer;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;

/**
 * SNS notifications handler that simply processes notification body.
 */
public class SimpleNotificationHandler extends NotificationHandler
{
    /**
     * Initializes notification handler.
     *
     * @param messageBodyHandler SNS message handler
     */
    public SimpleNotificationHandler(Consumer<String> messageBodyHandler)
    {
        super((SNSEvent.SNS data) -> messageBodyHandler.accept(data.getMessage()));
    }
}
