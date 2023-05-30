package edu.location.sharing.models.events.validation.user

enum class UserValidationPurpose(
    private val keys: List<AdditionalInfoKey>
) {
    GROUP_ADD_USER(listOf(
        AdditionalInfoKey.GROUP_ID
    )),

    GROUP_CHANGE_OWNER(listOf(
        AdditionalInfoKey.GROUP_ID
    ))
}

enum class AdditionalInfoKey(
    val keyName: String
) {
    GROUP_ID("groupId")
}