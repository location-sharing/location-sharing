package edu.location.sharing.events.connections

sealed class ConnectionEvent {
    abstract val userId: String
    abstract val groupId: String
    abstract val connectionId: String
}
