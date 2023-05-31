package edu.messaging.consumers

import edu.messaging.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.ReceiverOptions

abstract class GenericConsumer(
    kafkaConfig: KafkaConfig,
    topic: String
){
    abstract val log: Logger
    protected val consumer: ReactiveKafkaConsumerTemplate<String, ByteArray>

    companion object {
        private val stringDeserializer = StringDeserializer()
        private val byteArrayDeserializer = ByteArrayDeserializer()
    }

    init {
        val consumerProps = kafkaConfig.kafkaProperties.buildConsumerProperties()
        val receiverOptions = ReceiverOptions.create<String, ByteArray>(consumerProps)
            .withKeyDeserializer(stringDeserializer)
            .withValueDeserializer(byteArrayDeserializer)
            .subscription(listOf(topic))
        consumer = ReactiveKafkaConsumerTemplate(receiverOptions)
    }

    private fun doCreateFlux(): Flux<ConsumerRecord<String, ByteArray>> {
        log.info("creating event flux in ${this::class.qualifiedName}")
        return consumer
            .receiveAutoAck()
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { log.info("received record $it") }
    }

    fun createFlux(): Flux<*> {
        // retry on any error
        return processFlux(doCreateFlux())
//            .retry()
    }

    abstract fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*>
}