package edu.location.sharing.events.notifications

class SystemNotification(
    val type: Type,
    val userId: String? = null,
    val username: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
) {

    enum class Type {
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,
        GROUP_CREATE,
        GROUP_UPDATE,
        GROUP_DELETE,
        GROUP_USER_ADD,
        GROUP_USER_DELETE,
        GROUP_CHANGE_OWNER,
    }

    override fun toString(): String {
        return """
            SystemNotification {
                type: $type,
                userId: $userId,
                groupId: $groupId,
            }
        """.trimIndent()
    }
}