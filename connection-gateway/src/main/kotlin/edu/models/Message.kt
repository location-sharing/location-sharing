package edu.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val userId: String,
    val groupId: String,
    val content: String
)
