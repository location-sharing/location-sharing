package edu.messaging.producers

import edu.config.KafkaConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import java.time.Instant


abstract class GenericProducer(
    kafkaConfig: KafkaConfig,
){
    abstract val log: Logger
    protected val producer: ReactiveKafkaProducerTemplate<String, ByteArray>

    companion object {
        private val stringSerializer = StringSerializer()
        private val byteArraySerializer = ByteArraySerializer()
    }

    init {
        val producerProps = kafkaConfig.kafkaProperties.buildProducerProperties()
        val senderOptions = SenderOptions.create<String, ByteArray>(producerProps)
            .withKeySerializer(stringSerializer)
            .withValueSerializer(byteArraySerializer)
        producer = ReactiveKafkaProducerTemplate(senderOptions)
    }

    private fun logErrors(topic: String, error: Throwable) {
        log.error("error while sending record to topic $topic", error)
    }

    private fun logResults(result: SenderResult<*>) {
        val metadata = result.recordMetadata()
        val record = result.correlationMetadata()
        val time = Instant.ofEpochMilli(metadata.timestamp())
        log.info(
            "record $record sent successfully, topic=${metadata.topic()} partition=${metadata.partition()} " +
                    "offset=${metadata.offset()} timestamp=$time"
        )
    }

    protected fun <V> send(record: SenderRecord<String, ByteArray, V>): Mono<SenderResult<V>> {
        return producer.send(record)
            .publishOn(Schedulers.parallel())
            .doOnError { logErrors(record.topic(), it) }
    }

    protected fun <V> sendWithResultLogging(record: SenderRecord<String, ByteArray, V>):
            Mono<SenderResult<V>> {
        return send(record)
            .doOnNext { logResults(it) }
    }
}