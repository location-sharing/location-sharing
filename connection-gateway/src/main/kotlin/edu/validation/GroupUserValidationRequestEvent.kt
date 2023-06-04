package edu.validation

data class GroupUserValidationRequestEvent(
    val groupId: String,
    val userId: String,
    val metadata: GroupUserValidationMetadata,
)