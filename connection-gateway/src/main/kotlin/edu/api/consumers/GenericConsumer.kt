package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.models.events.headers.EventTypeKafkaHeader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import java.time.Duration
import java.time.Duration.ofMillis

abstract class GenericConsumer(
    topic: String,
    private val pollTimeout: Duration = ofMillis(100),
    additionalKafkaOptions: Map<String, Any?> = emptyMap(),
) {
    abstract val log: Logger
    private val consumer: KafkaConsumer<String, ByteArray>

    companion object {
        private val stringDeserializer = StringDeserializer()
        private val byteArrayDeserializer = ByteArrayDeserializer()
    }

    init {
        val kafkaOptions = KafkaConfig.kafkaOptions.toMutableMap()
        kafkaOptions.putAll(additionalKafkaOptions)
        consumer = KafkaConsumer(kafkaOptions, stringDeserializer, byteArrayDeserializer)
        consumer.subscribe(listOf(topic))
    }

    private val handler = CoroutineExceptionHandler {ctx, e ->
        log.error("error in context $ctx", e)
    }

    suspend fun receive() {
        log.info("starting consumer")

        coroutineScope {
            // dispatch blocking 'consume' task on a shared IO thread pool
            launch(Dispatchers.IO + handler) {
                consumer.use {
                    while (true) {
                        consumer
                            .poll(pollTimeout)
                            .forEach {
                                log.info("received record: $it")
                                processRecord(it)
                           }
                    }
                }
            }
        }
    }

    private suspend fun processRecord(record: ConsumerRecord<String, ByteArray>) {
        // execute the processing of messages on the default thread pool
        // the processing inside should be non-blocking
        coroutineScope {
            launch(Dispatchers.Default) {
                val eventType = EventTypeKafkaHeader.getEventType(record)
                if (eventType != null) {
                    process(eventType, record.value())
                }
            }
        }
    }

    protected abstract suspend fun process(eventType: EventType, data: ByteArray)
}