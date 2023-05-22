package edu.api.producers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord

object ClientMessageProducer: GenericProducer() {
    override val log = logger()

    fun send(event: ClientMessageEvent) {
        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val producerRecord = ProducerRecord<String, ByteArray>(
            KafkaConfig.clientMessageOutboundTopic,
            null,
            null,
            jsonBytes,
            listOf(EventTypeKafkaHeader(EventType.CLIENT_MESSAGE))
        )
        sendEvent(producerRecord)
    }
}