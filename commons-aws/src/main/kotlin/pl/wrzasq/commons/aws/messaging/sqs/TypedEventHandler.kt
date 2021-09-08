/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.messaging.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import pl.wrzasq.commons.aws.messaging.MessageHandler

/**
 * SQS event handler that processes typed message.
 *
 * @param objectMapper JSON handler.
 * @param messageHandler Single message consumer.
 * @param type Message content type.
 * @param <Type> Message type.
 */
class TypedEventHandler<Type>(
    objectMapper: ObjectMapper,
    messageHandler: (Type) -> Unit,
    type: Class<Type>
) : SimpleEventHandler(MessageHandler(objectMapper, messageHandler, type)::handle)
