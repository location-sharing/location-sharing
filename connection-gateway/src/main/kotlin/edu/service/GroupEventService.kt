package edu.service

import edu.api.producers.GroupEventProducer
import edu.models.GroupEvent
import edu.models.GroupEventType

object GroupEventService {

    fun sendMessageEvent(groupId: String, userId: String, content: String?) {
        content ?: return
        val messageEvent = GroupEvent(
            groupId,
            userId,
            GroupEventType.MESSAGE,
            content
        )
        GroupEventProducer.sendGroupEvent(messageEvent)
    }

    fun sendConnectedNotificationEvent(groupId: String, userId: String) {
        val notification = GroupEvent(
            groupId,
            userId,
            GroupEventType.CONNECTED_NOTIFICATION
        )
        GroupEventProducer.sendGroupEvent(notification)
    }

    fun sendDisconnectedNotificationEvent(groupId: String, userId: String) {
        val notification = GroupEvent(
            groupId,
            userId,
            GroupEventType.DISCONNECTED_NOTIFICATION
        )
        GroupEventProducer.sendGroupEvent(notification)
    }
}