package edu.api.producers

import edu.api.stringSerializer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.ConnectionEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper

object ConnectionEventProducer: GenericProducer<String, ConnectionEvent>(
    keySerializer = stringSerializer,
    valueSerializer = { _, data -> objectMapper.writeValueAsBytes(data) }
) {
    override val log = logger()

    fun sendStoreConnectionEvent(event: StoreConnectionEvent) {
        sendEvent(event, KafkaConfig.storeConnectionTopic)
    }

    fun sendRemoveConnectionEvent(event: RemoveConnectionEvent) {
        sendEvent(event, KafkaConfig.removeConnectionTopic)
    }
}