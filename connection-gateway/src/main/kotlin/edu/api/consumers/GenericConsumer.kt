package edu.api.consumers

import edu.config.KafkaConfig
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.Logger
import java.time.Duration
import java.time.Duration.ofMillis

abstract class GenericConsumer<K, V>(
    topic: String,
    private val pollTimeout: Duration = ofMillis(100),
    additionalKafkaOptions: Map<String, Any?> = emptyMap(),
    keyDeserializer: Deserializer<K>? = null,
    valueDeserializer: Deserializer<V>? = null,
) {
    abstract val log: Logger
    private val consumer: KafkaConsumer<K, V>

    init {
        val kafkaOptions = KafkaConfig.kafkaOptions.toMutableMap()
        kafkaOptions.putAll(additionalKafkaOptions)
        consumer = KafkaConsumer(kafkaOptions, keyDeserializer, valueDeserializer)
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
                                // execute the processing of messages on the default thread pool
                                // the processing inside should be non-blocking
                                launch(Dispatchers.IO) {
                                    process(it)
                                }
                           }
                    }
                }
            }
        }
    }

    protected abstract suspend fun process(record: ConsumerRecord<K, V>)
}