package edu.messaging.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.connections.headers.EventType
import edu.location.sharing.models.events.connections.headers.EventTypeKafkaHeader
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

    private fun doCreateFlux(): Flux<Pair<EventType, ByteArray>> {
        log.info("creating event flux in ${this::class.qualifiedName}")
        return consumer
            .receiveAutoAck()
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { log.info("received record $it") }
            .mapNotNull { record ->
                val eventType = EventTypeKafkaHeader.getEventType(record)
                if (eventType != null) {
                    return@mapNotNull Pair(eventType, record.value())
                }
                return@mapNotNull null
            }
    }

    fun createFlux(): Flux<*> {
        return processFlux(doCreateFlux())
            .retry()
    }

    abstract fun processFlux(flux: Flux<Pair<EventType, ByteArray>>): Flux<*>
}