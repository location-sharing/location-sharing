package edu.service

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.models.Message
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

object ClientMessageSender {

    private val log = logger()
    private val producer = KafkaProducer<String, Message>(KafkaConfig.kafkaOptions)

    fun sendMessage(event: Message) {
        producer.send(
            ProducerRecord(KafkaConfig.clientMessageTopic, null, event)
        ) {
                metadata, ex ->
            if (ex != null) {
                log.warn("error sending record $metadata: {}", ex)
            } else {
                log.debug("sent record $metadata")
            }
        }
    }
}