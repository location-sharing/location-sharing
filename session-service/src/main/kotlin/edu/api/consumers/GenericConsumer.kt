package edu.api.consumers

import edu.config.KafkaConfig
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.Logger
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.ReceiverOptions

abstract class GenericConsumer<K, V>(
    kafkaConfig: KafkaConfig,
    topic: String,
    valueDeserializer: Deserializer<V>
){
    abstract val log: Logger
    protected val consumer: ReactiveKafkaConsumerTemplate<K, V>

    init {
        val consumerProps = kafkaConfig.kafkaProperties.buildConsumerProperties()
        val receiverOptions = ReceiverOptions.create<K, V>(consumerProps)
            .withValueDeserializer(valueDeserializer)
            .subscription(listOf(topic))
        consumer = ReactiveKafkaConsumerTemplate(receiverOptions)
    }

    private fun doCreateFlux(): Flux<V> {
        log.info("creating event flux in ${this::class.qualifiedName}")
        return consumer
            .receiveAutoAck()
            .publishOn(Schedulers.parallel())
            .doOnNext { log.info("received record $it") }
            .map { it.value() }
    }

    fun createFlux(): Flux<Any> {
        // retry on any error
        return processFlux(doCreateFlux())
            .retry()
    }

    abstract fun processFlux(flux: Flux<V>): Flux<Any>
}