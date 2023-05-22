package edu.api.producers

import edu.api.stringSerializer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.ConnectionEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders

object ConnectionEventProducer: GenericProducer<String, ConnectionEvent>(
    keySerializer = stringSerializer,
    valueSerializer = { _, data -> objectMapper.writeValueAsBytes(data) }
) {
    override val log = logger()

    fun sendStoreConnectionEvent(event: StoreConnectionEvent) {
        sendEvent(
            event = event,
            topic = KafkaConfig.storeConnectionTopic,
            headers = listOf(
                EventTypeKafkaHeader(EventType.STORE_CONNECTION)
            )
        )
    }

    fun sendRemoveConnectionEvent(event: RemoveConnectionEvent) {
        sendEvent(
            event = event,
            topic = KafkaConfig.removeConnectionTopic,
            headers = listOf(
                EventTypeKafkaHeader(EventType.REMOVE_CONNECTION)
            )
        )
    }
}