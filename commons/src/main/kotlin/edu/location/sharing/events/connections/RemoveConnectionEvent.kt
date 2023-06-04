package edu.location.sharing.events.connections

data class RemoveConnectionEvent(
    override val userId: String,
    override val groupId: String,
    override val connectionId: String,
) : ConnectionEvent()