package edu.messaging.consumers

import edu.config.KafkaConfig
import edu.location.sharing.events.validation.group.user.GroupUserValidationPurpose
import edu.location.sharing.events.validation.group.user.GroupUserValidationResultEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper
import edu.validation.GroupUserValidationService
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

object GroupUserValidationResultConsumer : GenericConsumer(
    KafkaConfig.groupUserValidationResultTopic,
    Duration.ofMillis(300000),
) {
    override val log = logger()

    override suspend fun process(record: ConsumerRecord<String, ByteArray>) {
        val event = objectMapper.readValue(record.value(), GroupUserValidationResultEvent::class.java)

        if (event.metadata.purpose != GroupUserValidationPurpose.CONNECTION_CREATE) {
            // validation request not sent by any instance of this service
            return
        }
        log.info("consuming group user validation event: $event")
        GroupUserValidationService.consumeResult(event)
    }
}