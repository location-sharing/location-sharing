package edu.messaging.producers

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.util.objectMapper
import edu.validation.GroupUserValidationRequestEvent
import org.apache.kafka.clients.producer.ProducerRecord

object GroupUserValidationRequestProducer : GenericProducer() {

    override val log = logger()

    fun sendValidationRequest(event: GroupUserValidationRequestEvent) {
        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val producerRecord = ProducerRecord<String, ByteArray>(
            KafkaConfig.groupUserValidationRequestTopic,
            null,
            null,
            jsonBytes,
        )
        sendEvent(producerRecord)
    }
}