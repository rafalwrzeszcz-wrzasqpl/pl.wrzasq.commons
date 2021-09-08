/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sns

/**
 * SNS notifications handler that simply processes notification body.
 *
 * @param messageBodyHandler SNS message handler
 */
open class SimpleNotificationHandler(
    messageBodyHandler: (String) -> Unit
) : NotificationHandler({ messageBodyHandler(it.message) })
