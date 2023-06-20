package edu.service

import edu.messaging.producers.GroupEventProducer
import edu.models.GroupEvent
import edu.models.GroupEventType
import edu.security.AuthenticatedUser

object GroupEventService {

    fun sendMessageEvent(groupId: String, user: AuthenticatedUser, content: String?) {
        content ?: return
        val messageEvent = GroupEvent(
            groupId,
            user.id,
            user.username,
            GroupEventType.MESSAGE,
            content
        )
        GroupEventProducer.sendGroupEvent(messageEvent)
    }

    fun sendConnectedNotificationEvent(groupId: String, user: AuthenticatedUser) {
        val notification = GroupEvent(
            groupId,
            user.id,
            user.username,
            GroupEventType.CONNECTED_NOTIFICATION
        )
        GroupEventProducer.sendGroupEvent(notification)
    }

    fun sendDisconnectedNotificationEvent(groupId: String, user: AuthenticatedUser) {
        val notification = GroupEvent(
            groupId,
            user.id,
            user.username,
            GroupEventType.DISCONNECTED_NOTIFICATION
        )
        GroupEventProducer.sendGroupEvent(notification)
    }
}