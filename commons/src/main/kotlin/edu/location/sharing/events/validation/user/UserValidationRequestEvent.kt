package edu.location.sharing.events.validation.user

import edu.location.sharing.events.validation.user.UserValidationMetadata

data class UserValidationRequestEvent(
    val resourceId: String,
    val metadata: UserValidationMetadata,
)