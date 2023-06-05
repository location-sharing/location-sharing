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

    fun userGroupConnectionsEmpty(groupId: String, userId: String): Boolean {
        return ConnectionStore.userGroupConnectionsEmpty(groupId, userId)
    }

    fun isTheOnlyConnection(groupId: String, userId: String, connection: WebSocketSession): Boolean {
        return ConnectionStore.isTheOnlyConnection(groupId, userId, connection)
    }
}