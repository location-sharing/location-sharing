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

    @Value("\${client_messages.inbound.topic}")
    val clientMessagesInboundTopic: String,

    @Value("\${client_messages.outbound.topic}")
    val clientMessagesOutboundTopic: String,

    val kafkaProperties: KafkaProperties,
)