package edu.validation

data class GroupUserValidationResultEvent(
    val groupId: String,
    val userId: String,
    val metadata: GroupUserValidationMetadata,
    val valid: Boolean,
    val message: String? = null
)