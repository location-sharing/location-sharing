package edu.messaging.producers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult

@Component
class ClientMessageProducer(
    private val kafkaConfig: KafkaConfig
): GenericProducer(
    kafkaConfig,
) {
    override val log = logger()

    fun sendWithResultLogging(event: ClientMessageEvent): Mono<SenderResult<ClientMessageEvent>> {

        val jsonBytes = objectMapper.writeValueAsBytes(event)

        // must create a ProducerRecord beforehand to add headers... (SenderRecord doesn't know headers)
        val producerRecord = ProducerRecord<String, ByteArray>(
            kafkaConfig.clientMessagesOutboundTopic,
            null,
            null,
            jsonBytes,
            listOf(EventTypeKafkaHeader(EventType.CLIENT_MESSAGE))
        )

        return super.sendWithResultLogging(SenderRecord.create(producerRecord, event))
    }
}