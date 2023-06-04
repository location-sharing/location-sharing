package edu.location.sharing.events.validation.group.user

data class GroupUserValidationRequestEvent(
    val groupId: String,
    val userId: String,
    val metadata: GroupUserValidationMetadata,
)