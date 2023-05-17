package edu.location.sharing.models.events

sealed class ConnectionEvent {
    abstract val groupId: String
    abstract val connectionId: String
}
