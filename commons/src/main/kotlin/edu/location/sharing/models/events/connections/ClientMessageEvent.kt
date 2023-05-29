package edu.location.sharing.models.events.connections

data class ClientMessageEvent(
    val userId: String,
    val groupId: String,
    val connectionId: String,
    val content: String,
)