package edu.api.producers

import edu.api.stringSerializer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper

object ClientMessageProducer: GenericProducer<String, ClientMessageEvent>(
    keySerializer = stringSerializer,
    valueSerializer = { _, data -> objectMapper.writeValueAsBytes(data) }
) {
    override val log = logger()

    fun send(event: ClientMessageEvent) {
        sendEvent(event, KafkaConfig.clientMessageOutboundTopic)
    }
}