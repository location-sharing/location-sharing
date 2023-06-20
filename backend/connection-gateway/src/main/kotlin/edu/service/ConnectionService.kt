package edu.service

import edu.repository.ConnectionStore
import edu.security.AuthenticatedUser
import io.ktor.websocket.*

object ConnectionService {

    fun storeConnection(groupId: String, user: AuthenticatedUser, session: WebSocketSession) {
        ConnectionStore.storeConnection(groupId, user, session)
    }

    fun removeConnection(groupId: String, user: AuthenticatedUser, session: WebSocketSession) {
        ConnectionStore.removeConnection(groupId, user, session)
    }

    fun userGroupConnectionsEmpty(groupId: String, user: AuthenticatedUser): Boolean {
        return ConnectionStore.userGroupConnectionsEmpty(groupId, user)
    }

    fun isTheOnlyConnection(groupId: String, user: AuthenticatedUser, connection: WebSocketSession): Boolean {
        return ConnectionStore.isTheOnlyConnection(groupId, user, connection)
    }
}