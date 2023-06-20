package edu.location.sharing.events.validation.group.user

import edu.location.sharing.events.validation.group.user.GroupUserValidationMetadata

data class GroupUserValidationResultEvent(
    val groupId: String,
    val userId: String,
    val metadata: GroupUserValidationMetadata,
    val valid: Boolean,
    val message: String? = null
)