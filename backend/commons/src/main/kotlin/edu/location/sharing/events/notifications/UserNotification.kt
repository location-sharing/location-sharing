package edu.location.sharing.events.notifications

class UserNotification(
    val type: Type,
    val title: String,
    val message: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val groupId: String? = null,
    val groupName: String?= null,
) {

    enum class Type {
        SUCCESS, ERROR
    }

    override fun toString(): String {
        return """
            UserNotification {
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
