package edu.models

data class GroupEvent(
    val groupId: String,
    val userId: String,
    val type: GroupEventType,
    val payload: String? = null
)

enum class GroupEventType {
    CONNECTED_NOTIFICATION,
    DISCONNECTED_NOTIFICATION,
    MESSAGE;
}