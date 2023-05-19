package edu.api.consumers

import edu.api.stringDeserializer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.util.logger
import edu.models.Message
import edu.service.MessageSender
import edu.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration.ofMillis

object ClientMessageConsumer: GenericConsumer<String, ClientMessageEvent>(
    KafkaConfig.clientMessageInboundTopic,
    pollTimeout = ofMillis(300000),
    keyDeserializer = stringDeserializer,
    valueDeserializer = { _, data -> objectMapper.readValue(data, ClientMessageEvent::class.java) }
) {

    override val log = logger()

    override suspend fun process(record: ConsumerRecord<String, ClientMessageEvent>) {
        log.info("received message: ${record.value()}")

        val messageEvent = record.value()
        val message = Message(
            messageEvent.userId,
            messageEvent.groupId,
            messageEvent.content
        )

        MessageSender.sendToConnection(messageEvent.connectionId, message)
    }
}