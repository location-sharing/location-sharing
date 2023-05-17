package edu.service

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.util.logger
import io.ktor.util.logging.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

object ConnectionEventSender {

    private val log = logger()
    private val producer = KafkaProducer<String, ConnectionEvent>(KafkaConfig.kafkaOptions)

    private fun sendConnectionEvent(event: ConnectionEvent, topic: String, key: String? = null) {
        producer.send(
            ProducerRecord(topic, key, event)
        ) {
                metadata, ex ->
            if (ex != null) {
                log.warn("error sending record $metadata: {}", ex)
            } else {
                log.debug("sent record $metadata")
            }
        }
    }

    fun sendStoreConnectionEvent(event: StoreConnectionEvent) {
        sendConnectionEvent(event, KafkaConfig.storeConnectionTopic)
    }

    fun sendRemoveConnectionEvent(event: RemoveConnectionEvent) {
        sendConnectionEvent(event, KafkaConfig.removeConnectionTopic)
    }
}