package edu.api.producers

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.models.GroupEvent
import edu.util.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord

object GroupEventProducer: GenericProducer() {
    override val log = logger()

//    fun sendStoreConnectionEvent(event: StoreConnectionEvent) {
//        sendConnectionEvent(event, EventType.STORE_CONNECTION)
//    }

    fun sendGroupEvent(event: GroupEvent) {
        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val producerRecord = ProducerRecord<String, ByteArray>(
            KafkaConfig.groupEventsTopic,
            null,
            null,
            jsonBytes,
        )
        sendEvent(producerRecord)
    }

//    fun sendRemoveConnectionEvent(event: RemoveConnectionEvent) {
//        sendConnectionEvent(event, EventType.REMOVE_CONNECTION)
//    }
//
//    private fun sendConnectionEvent(event: ConnectionEvent, eventType: EventType) {
//        val jsonBytes = objectMapper.writeValueAsBytes(event)
//
//        val producerRecord = ProducerRecord<String, ByteArray>(
//            KafkaConfig.connectionEventsTopic,
//            null,
//            null,
//            jsonBytes,
//            listOf(EventTypeKafkaHeader(eventType))
//        )
//        sendEvent(producerRecord)
//    }
}