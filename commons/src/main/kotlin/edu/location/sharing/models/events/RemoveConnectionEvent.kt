package edu.location.sharing.models.events

data class RemoveConnectionEvent(
    override val userId: String,
    override val groupId: String,
    override val connectionId: String,
) : ConnectionEvent()