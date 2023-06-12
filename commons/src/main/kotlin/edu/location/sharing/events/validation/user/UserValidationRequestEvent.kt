package edu.location.sharing.events.validation.user

import edu.location.sharing.events.validation.user.UserValidationMetadata

data class UserValidationRequestEvent(
    val userId: String? = null,
    val username: String? = null,
    val metadata: UserValidationMetadata,
)