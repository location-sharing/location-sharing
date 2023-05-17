package edu.location.sharing.models.events

data class StoreConnectionEvent(
    override val groupId: String,
    override val connectionId: String,
    val receiveTopic: String
) : ConnectionEvent()


