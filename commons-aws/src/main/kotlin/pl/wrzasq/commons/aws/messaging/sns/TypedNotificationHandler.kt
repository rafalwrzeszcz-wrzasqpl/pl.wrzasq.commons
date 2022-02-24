/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2019, 2021 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sns

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pl.wrzasq.commons.aws.messaging.MessageHandler

/**
 * SNS notifications handler that processes typed message.
 *
 * @param json JSON handler.
 * @param messageHandler Single message consumer.
 * @param typeSerializer Expected message type handler.
 * @param <Type> Message type.
 */
class TypedNotificationHandler<Type>(
    json: Json,
    messageHandler: (Type) -> Unit,
    typeSerializer: KSerializer<Type>
) : SimpleNotificationHandler(MessageHandler(json, messageHandler, typeSerializer)::handle)
