/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sqs

/**
 * SQS event handler that simply processes plain message body.
 *
 * @param messageBodyHandler SQS message handler
 */
open class SimpleEventHandler(
    messageBodyHandler: (String) -> Unit
) : EventHandler({ messageBodyHandler(it.body) })
