package edu.config

import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.RemoveConnectionEventDeserializer
import edu.location.sharing.util.StoreConnectionEventDeserializer
import org.apache.kafka.common.serialization.Deserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class KafkaConfig(

    @Value("\${store_connection.topic}")
    val storeConnectionTopic: String,

    @Value("\${remove_connection.topic}")
    val removeConnectionTopic: String,

    val kafkaProperties: KafkaProperties,
) {

    fun <K, V> getConsumer(topic: String, valueDeserializer: Deserializer<V>): ReactiveKafkaConsumerTemplate<K, V> {
        val receiverOptions = ReceiverOptions.create<K, V>(kafkaProperties.buildConsumerProperties())
            .withValueDeserializer(valueDeserializer)
            .subscription(listOf(topic))
        return ReactiveKafkaConsumerTemplate(receiverOptions)
    }

    @Bean
    fun storeConnectionConsumerTemplate(): ReactiveKafkaConsumerTemplate<String, StoreConnectionEvent> =
        getConsumer(storeConnectionTopic, StoreConnectionEventDeserializer)

    @Bean
    fun removeConnectionConsumerTemplate(): ReactiveKafkaConsumerTemplate<String, RemoveConnectionEvent> =
        getConsumer(removeConnectionTopic, RemoveConnectionEventDeserializer)
}