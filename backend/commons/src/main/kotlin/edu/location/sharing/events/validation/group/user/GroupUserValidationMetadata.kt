package edu.location.sharing.events.validation.group.user

data class GroupUserValidationMetadata(
    val initiatorId: String,
    val validationRequestId: String,
    val purpose: GroupUserValidationPurpose,
)

enum class GroupUserValidationPurpose {
    CONNECTION_CREATE
}