package edu.models

data class ClientMessage(
    val userId: String,
    val groupId: String,
    val content: String,
)
