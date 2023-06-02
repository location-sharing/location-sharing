package edu.config

import io.ktor.server.config.*

object KafkaConfig {

    private val config = ApplicationConfig("application.yaml")
    val kafkaOptions: Map<String, Any?> = config.config("kafka").toMap()

    val groupEventsTopic = config.property("kafka_topics.group_events_topic").getString()
}
