package edu.locationsharing.models

class SystemNotification(
    val type: Type,
    val message: String?,
    val additionalInfo: Map<AdditionalInfoKey, String>?
) {
    enum class Type {
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED,
        GROUP_CREATED,
        GROUP_UPDATED,
        GROUP_DELETED,
        GROUP_USER_ADDED,
        GROUP_USER_REMOVED,
        GROUP_OWNER_CHANGED,
        DIRECT // can be directly sent to a client
    }

    enum class AdditionalInfoKey {
        USER_ID,
        GROUP_ID,
    }

    override fun toString(): String {
        return """
            SystemNotification {
                type: $type,
                message: $message,
                additionalInfo: ${additionalInfo.toString()}
            }
        """.trimIndent()
    }
}
