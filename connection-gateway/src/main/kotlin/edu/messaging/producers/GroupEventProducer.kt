package edu.messaging.producers

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.models.GroupEvent
import edu.util.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord

object GroupEventProducer : GenericProducer() {
    override val log = logger()

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
}