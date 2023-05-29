package edu.messaging.events

data class UserValidationResponseEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
    val valid: Boolean,
    val message: String? = null,
    val user: UserEvent?,
)