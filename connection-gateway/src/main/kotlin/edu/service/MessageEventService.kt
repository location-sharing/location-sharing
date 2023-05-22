package edu.service

import edu.api.producers.ClientMessageProducer
import edu.location.sharing.models.events.ClientMessageEvent
import edu.models.Message
import java.util.*

object MessageEventService {

    fun sendClientMessageEvent(connectionId: UUID, message: Message) = ClientMessageProducer.send(
        ClientMessageEvent(
            message.userId,
            message.groupId,
            connectionId.toString(),
            message.content
        )
    )
}