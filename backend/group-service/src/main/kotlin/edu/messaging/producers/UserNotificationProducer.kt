package edu.messaging.producers

import edu.location.sharing.events.notifications.UserNotification
import edu.messaging.config.KafkaConfig
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult

@Component
class UserNotificationProducer(
    private val kafkaConfig: KafkaConfig
): GenericProducer(kafkaConfig) {

    override val log = logger()

    suspend fun sendWithResultLogging(event: UserNotification): SenderResult<UserNotification> {

        val jsonBytes = objectMapper.writeValueAsBytes(event)

        val record = ProducerRecord<String, ByteArray>(
            kafkaConfig.userNotificationsTopic,
            null,
            null,
            jsonBytes
        )

        log.info("sending user notification")

        return super.sendWithResultLogging(SenderRecord.create(record, event)).awaitSingle()
    }
}