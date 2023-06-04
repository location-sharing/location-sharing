package edu.location.sharing.events.connections

data class ClientMessageEvent(
    val userId: String,
    val groupId: String,
    val connectionId: String,
    val content: String,
)