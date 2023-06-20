package edu.messaging.producers

import edu.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger

abstract class GenericProducer(
    additionalKafkaOptions: Map<String, Any?> = emptyMap(),
) {

    abstract val log: Logger
    private val producer: KafkaProducer<String, ByteArray>

    companion object {
        private val stringSerializer = StringSerializer()
        private val byteArraySerializer = ByteArraySerializer()
    }

    init {
        val kafkaOptions = KafkaConfig.kafkaOptions.toMutableMap()
        kafkaOptions.putAll(additionalKafkaOptions)
        producer = KafkaProducer(kafkaOptions, stringSerializer, byteArraySerializer)
    }

    protected fun sendEvent(record: ProducerRecord<String, ByteArray>) {
        producer.send(record) { metadata, ex ->
            if (ex != null) {
                log.warn("error sending record $metadata: {}", ex)
            } else {
                log.debug("sent record $metadata")
            }
        }
    }
}