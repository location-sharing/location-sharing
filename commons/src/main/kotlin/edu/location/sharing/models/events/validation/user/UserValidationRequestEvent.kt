package edu.location.sharing.models.events.validation.user

import edu.location.sharing.models.events.validation.user.UserValidationMetadata

data class UserValidationRequestEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
)