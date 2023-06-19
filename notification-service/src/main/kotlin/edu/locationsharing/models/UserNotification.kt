package edu.locationsharing.models

class UserNotification(
    val title: String,
    val message: String?,
    val userId: String,
    val groupId: String?,
    val groupName: String?,
) {
    override fun toString(): String {
        return """
            SystemNotification {
                title: $title,
                message: $message,
                userId: $userId,
                groupId: $groupId,
                groupName: $groupName
            }
        """.trimIndent()
    }
}
