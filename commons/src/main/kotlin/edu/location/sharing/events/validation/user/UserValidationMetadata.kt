package edu.location.sharing.events.validation.user

/**
 * Metadata to be sent along with a message so the intended recipient knows what and how to do
 * when it receives a response with the same metadata
 */
data class UserValidationMetadata(
    val initiatorUserId: String,
    val purpose: UserValidationPurpose,
    val additionalInfo: Map<AdditionalInfoKey, String>
)