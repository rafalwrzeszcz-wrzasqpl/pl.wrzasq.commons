/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.sqs;

import java.util.function.Consumer;

/**
 * SQS event handler that simply processes plain message body.
 */
public class SimpleEventHandler extends EventHandler {
    /**
     * Initializes notification handler.
     *
     * @param messageBodyHandler SQS message handler
     */
    public SimpleEventHandler(Consumer<String> messageBodyHandler) {
        super(data -> messageBodyHandler.accept(data.getBody()));
    }
}
