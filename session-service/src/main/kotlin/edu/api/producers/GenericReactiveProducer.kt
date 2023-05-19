package edu.api.producers

import edu.config.KafkaConfig
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.Logger
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import java.time.Instant


abstract class GenericReactiveProducer<K, V>(
    kafkaConfig: KafkaConfig,
    valueSerializer: Serializer<V>,
){
    abstract val log: Logger
    protected val producer: ReactiveKafkaProducerTemplate<K, V>

    init {
        val producerProps = kafkaConfig.kafkaProperties.buildProducerProperties()
        val senderOptions = SenderOptions.create<K, V>(producerProps)
            .withValueSerializer(valueSerializer)
        producer = ReactiveKafkaProducerTemplate(senderOptions)
    }

    private fun logErrors(topic: String, error: Throwable) {
        log.error("error while sending record to topic $topic", error)
    }

    private fun logResults(result: SenderResult<V>) {
        val metadata = result.recordMetadata()
        val record = result.correlationMetadata()
        val time = Instant.ofEpochMilli(metadata.timestamp())
        log.info(
            "record $record sent successfully, topic=${metadata.topic()} partition=${metadata.partition()} " +
                    "offset=${metadata.offset()} timestamp=$time"
        )
    }

    fun send(topic: String, value: V): Mono<SenderResult<V>> {
        return producer.send(
            SenderRecord.create<K, V, V>(topic, null, null, null, value, value)
        )
            .publishOn(Schedulers.parallel())
            .doOnError { logErrors(topic, it) }
    }

    fun sendWithResultLogging(topic: String, value: V): Mono<SenderResult<V>> {
        return send(topic, value)
            .doOnNext { logResults(it) }
    }
}