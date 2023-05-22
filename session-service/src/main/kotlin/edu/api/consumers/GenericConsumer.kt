package edu.api.consumers

import com.fasterxml.jackson.databind.JavaType
import edu.config.KafkaConfig
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import edu.util.kafkaByteArrayDeserializer
import edu.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.Logger
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.ReceiverOptions

//abstract class GenericConsumer<K, V>(
//    kafkaConfig: KafkaConfig,
//    topic: String,
//    valueDeserializer: Deserializer<V>
//){
//    abstract val log: Logger
//    protected val consumer: ReactiveKafkaConsumerTemplate<K, V>
//
//    init {
//        val consumerProps = kafkaConfig.kafkaProperties.buildConsumerProperties()
//        val receiverOptions = ReceiverOptions.create<K, V>(consumerProps)
//            .withValueDeserializer(valueDeserializer)
//            .subscription(listOf(topic))
//        consumer = ReactiveKafkaConsumerTemplate(receiverOptions)
//    }
//
//    private fun doCreateFlux(): Flux<V> {
//        log.info("creating event flux in ${this::class.qualifiedName}")
//        return consumer
//            .receiveAutoAck()
//            .publishOn(Schedulers.parallel())
//            .doOnNext { log.info("received record $it") }
//            .map { it.value() }
//    }
//
//    fun createFlux(): Flux<Any> {
//        // retry on any error
//        return processFlux(doCreateFlux())
//            .retry()
//    }
//
//    abstract fun processFlux(flux: Flux<V>): Flux<Any>
//}

abstract class GenericConsumer(
    kafkaConfig: KafkaConfig,
    topic: String
){
    abstract val log: Logger
    protected val consumer: ReactiveKafkaConsumerTemplate<String, ByteArray>

    init {
        val consumerProps = kafkaConfig.kafkaProperties.buildConsumerProperties()
        val receiverOptions = ReceiverOptions.create<String, ByteArray>(consumerProps)
            .withValueDeserializer(kafkaByteArrayDeserializer)
            .subscription(listOf(topic))
        consumer = ReactiveKafkaConsumerTemplate(receiverOptions)
    }

    private fun doCreateFlux(): Flux<Pair<EventType, ByteArray>> {
        log.info("creating event flux in ${this::class.qualifiedName}")
        return consumer
            .receiveAutoAck()
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { log.info("received record $it") }
            .mapNotNull { record ->
                val eventType = EventTypeKafkaHeader.getEventType(record)
                if (eventType != null) {
                    Pair(eventType, record.value())
                }
                null
            }
    }

    fun createFlux(): Flux<*> {
        // retry on any error
        return processFlux(doCreateFlux())
            .retry()
    }

    abstract fun processFlux(flux: Flux<Pair<EventType, ByteArray>>): Flux<*>
}