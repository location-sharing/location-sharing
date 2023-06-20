package edu.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(

    @Value("\${topics.connection_events}")
    val connectionEventsTopic: String,

    @Value("\${topics.client_messages.inbound}")
    val clientMessagesInboundTopic: String,

    @Value("\${topics.client_messages.outbound}")
    val clientMessagesOutboundTopic: String,

    val kafkaProperties: KafkaProperties,
)