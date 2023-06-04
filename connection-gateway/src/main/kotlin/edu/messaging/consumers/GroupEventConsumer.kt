package edu.messaging.consumers

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.models.GroupEvent
import edu.service.ClientMessageSender
import edu.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration.ofMillis

object GroupEventConsumer : GenericConsumer(
    KafkaConfig.groupEventsTopic,
    pollTimeout = ofMillis(300000),
) {
    override val log = logger()

    override suspend fun process(record: ConsumerRecord<String, ByteArray>) {
        val event = objectMapper.readValue(record.value(), GroupEvent::class.java)
        ClientMessageSender.sendToGroup(event)
    }
}