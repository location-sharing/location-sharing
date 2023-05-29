package edu.location.sharing.models.events.validation.user

data class UserValidationMetadata(
    val initiatorUserId: String,
    val purpose: UserValidationPurpose,
    val additionalInfo: Map<AdditionalInfoKey, String>
)