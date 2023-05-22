package edu.service

import edu.api.producers.ConnectionEventProducer
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.models.Connection
import edu.models.Message
import io.ktor.websocket.*
import java.util.*

object ConnectionService {

    fun storeConnection(session: WebSocketSession): UUID = ConnectionStore.put(Connection(session))
    fun removeConnection(connectionId: UUID) = ConnectionStore.remove(connectionId)

    fun sendStoreConnectionEvent(connectionId: UUID, message: Message) {
        ConnectionEventProducer.sendStoreConnectionEvent(
            StoreConnectionEvent(
                message.userId,
                message.groupId,
                connectionId.toString(),
            )
        )
    }

    fun sendRemoveConnectionEvent(connectionId: UUID, message: Message) {
        ConnectionEventProducer.sendRemoveConnectionEvent(
            RemoveConnectionEvent(
                message.userId,
                message.groupId,
                connectionId.toString(),
            )
        )
    }
}