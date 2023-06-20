package edu.config

import io.ktor.server.config.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.*

object KafkaConfig {

    private val config = ApplicationConfig("application.yaml")

    val consumerGroupId = config.property("kafka_consumer_group_id_prefix").getString()
    val kafkaOptions: MutableMap<String, Any?> = config.config("kafka").toMap().toMutableMap()

    val groupEventsTopic = config.property("kafka_topics.group_events_topic").getString()
    val groupUserValidationRequestTopic = config.property("kafka_topics.validation_group_user_request").getString()
    val groupUserValidationResultTopic = config.property("kafka_topics.validation_group_user_result").getString()

    init {
        kafkaOptions[ConsumerConfig.GROUP_ID_CONFIG] = "$consumerGroupId.${UUID.randomUUID()}"
    }
}
