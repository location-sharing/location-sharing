package edu.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(

    @Value("\${store_connection.topic}")
    val storeConnectionTopic: String,

    @Value("\${remove_connection.topic}")
    val removeConnectionTopic: String,

    @Value("\${client_messages.topic.inbound}")
    val clientMessagesInboundTopic: String,

    @Value("\${client_messages.topic.outbound}")
    val clientMessagesOutboundTopic: String,

    val kafkaProperties: KafkaProperties,
)