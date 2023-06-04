package edu.location.sharing.events.validation.user

data class UserValidationResultEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
    val valid: Boolean,
    val message: String? = null,
    val user: UserEvent?,
)