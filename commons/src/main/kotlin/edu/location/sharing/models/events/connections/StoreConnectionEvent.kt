package edu.location.sharing.models.events.connections

data class StoreConnectionEvent(
    override val userId: String,
    override val groupId: String,
    override val connectionId: String,
) : ConnectionEvent()


