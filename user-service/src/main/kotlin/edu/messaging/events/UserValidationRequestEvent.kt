package edu.messaging.events

data class UserValidationRequestEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
)