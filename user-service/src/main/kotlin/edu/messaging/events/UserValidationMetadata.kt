package edu.messaging.events

data class UserValidationMetadata(
    val initiatorUserId: String,
    val purpose: UserValidationPurpose,
    val additionalInfo: Map<AdditionalInfoKey, String>
)