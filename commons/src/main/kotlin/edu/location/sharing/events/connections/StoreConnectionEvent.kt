package edu.location.sharing.events.connections

data class StoreConnectionEvent(
    override val userId: String,
    override val groupId: String,
    override val connectionId: String,
) : ConnectionEvent()


