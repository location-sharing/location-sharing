package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.util.logger
import edu.models.Message
import edu.service.MessageSender
import edu.util.objectMapper
import java.time.Duration.ofMillis

object ClientMessageConsumer: GenericConsumer(
    KafkaConfig.clientMessageInboundTopic,
    pollTimeout = ofMillis(300000),
) {

    override val log = logger()

    override suspend fun process(eventType: EventType, data: ByteArray) {

        if (eventType != EventType.CLIENT_MESSAGE)
            return

        val messageEvent = objectMapper.readValue(data, ClientMessageEvent::class.java)
        val message = Message(
            messageEvent.userId,
            messageEvent.groupId,
            messageEvent.content
        )

        MessageSender.sendToConnection(messageEvent.connectionId, message)
    }
}