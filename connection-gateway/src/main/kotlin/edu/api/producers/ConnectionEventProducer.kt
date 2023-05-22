package edu.api.producers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ConnectionEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord

object ConnectionEventProducer: GenericProducer() {
    override val log = logger()

    fun sendStoreConnectionEvent(event: StoreConnectionEvent) {
        sendConnectionEvent(event, EventType.STORE_CONNECTION)
    }

    fun sendRemoveConnectionEvent(event: RemoveConnectionEvent) {
        sendConnectionEvent(event, EventType.REMOVE_CONNECTION)
    }

    private fun sendConnectionEvent(event: ConnectionEvent, eventType: EventType) {
        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val producerRecord = ProducerRecord<String, ByteArray>(
            KafkaConfig.connectionEventsTopic,
            null,
            null,
            jsonBytes,
            listOf(EventTypeKafkaHeader(eventType))
        )
        sendEvent(producerRecord)
    }
}