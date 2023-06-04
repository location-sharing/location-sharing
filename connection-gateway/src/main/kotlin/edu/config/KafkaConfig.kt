package edu.config

import io.ktor.server.config.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer

object KafkaConfig {

    private val config = ApplicationConfig("application.yaml")

    val consumerGroupId = config.property("kafka_consumer_group_id_prefix").getString()

    val kafkaOptions: Map<String, Any?> = config.config("kafka").toMap().apply {
        // set groupId to a random value, all consumers can use this
        toMutableMap()[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroupId
        toMap()
    }

    val groupEventsTopic = config.property("kafka_topics.group_events_topic").getString()

    val groupUserValidationRequestTopic = config.property("validation_group_user_request").getString()
    val groupUserValidationResultTopic = config.property("validation_group_user_result").getString()
}
