package edu.locationsharing.models

class UserNotification(
    val type: Type,
    val title: String,
    val message: String? = null,
    val userId: String,
    val groupId: String?= null,
    val groupName: String? = null,
) {

    enum class Type {
        SUCCESS, ERROR
    }

    override fun toString(): String {
        return """
            SystemNotification {
                type: $type,
                title: $title,
                message: $message,
                userId: $userId,
                groupId: $groupId,
                groupName: $groupName
            }
        """.trimIndent()
    }
}
