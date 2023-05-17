package edu.location.sharing.models.events

data class ClientMessageEvent(
    val userId: String,
    val groupId: String,
    val content: String,
)