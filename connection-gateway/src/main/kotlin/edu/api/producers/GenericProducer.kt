package edu.api.producers

import edu.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.Logger

abstract class GenericProducer<K, V>(
    additionalKafkaOptions: Map<String, Any?> = emptyMap(),
    keySerializer: Serializer<K>? = null,
    valueSerializer: Serializer<V>? = null,
) {

    abstract val log: Logger
    private val producer: KafkaProducer<K, V>

    init {
        val kafkaOptions = KafkaConfig.kafkaOptions.toMutableMap()
        kafkaOptions.putAll(additionalKafkaOptions)
        producer = KafkaProducer(kafkaOptions, keySerializer, valueSerializer)
    }

    fun sendEvent(
        event: V,
        topic: String,
        key: K? = null,
        partition: Int? = null,
        headers: Iterable<Header>? = null
    ) {
        producer.send(
            ProducerRecord(topic, partition, key, event, headers)
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