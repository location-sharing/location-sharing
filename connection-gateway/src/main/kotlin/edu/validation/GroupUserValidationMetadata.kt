package edu.validation

data class GroupUserValidationMetadata(
    val initiatorId: String,
    val validationRequestId: String,
    val purpose: GroupUserValidationPurpose,
)

enum class GroupUserValidationPurpose {
    CONNECTION_CREATE
}