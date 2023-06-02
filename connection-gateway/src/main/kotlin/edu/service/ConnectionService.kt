package edu.service

import edu.repository.ConnectionStore
import io.ktor.websocket.*

object ConnectionService {

    fun storeConnection(groupId: String, userId: String, session: WebSocketSession) {
        ConnectionStore.storeConnection(groupId, userId, session)
    }

    fun removeConnection(groupId: String, userId: String, session: WebSocketSession) {
        ConnectionStore.removeConnection(groupId, userId, session)
    }
}