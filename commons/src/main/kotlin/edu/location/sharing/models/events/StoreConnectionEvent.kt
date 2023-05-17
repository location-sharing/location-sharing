package edu.location.sharing.models.events

data class StoreConnectionEvent(
    override val userId: String,
    override val groupId: String,
    override val connectionId: String,
    override val receiveTopic: String
) : ConnectionEvent()


