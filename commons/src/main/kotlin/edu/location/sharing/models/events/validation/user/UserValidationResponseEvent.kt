package edu.location.sharing.models.events.validation.user

import edu.location.sharing.models.events.validation.user.UserEvent
import edu.location.sharing.models.events.validation.user.UserValidationMetadata

data class UserValidationResponseEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
    val valid: Boolean,
    val message: String? = null,
    val user: UserEvent?,
)