package edu.messaging.producers

import edu.location.sharing.events.validation.user.UserValidationRequestEvent
import edu.messaging.config.KafkaConfig
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult

@Component
class UserValidationRequestProducer(
    private val kafkaConfig: KafkaConfig
): GenericProducer(kafkaConfig) {

    override val log = logger()

    suspend fun sendWithResultLogging(event: UserValidationRequestEvent): SenderResult<UserValidationRequestEvent> {

        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val record = ProducerRecord<String, ByteArray>(
            kafkaConfig.userValidationRequestTopic,
            null,
            null,
            jsonBytes
        )

        return super.sendWithResultLogging(SenderRecord.create(record, event))
            .doOnNext {
                log.info("sent user validation request $event")
            }
            .awaitSingle()
    }
}