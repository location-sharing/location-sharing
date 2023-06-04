package edu.messaging.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(

    @Value("\${topics.validation.user.request}")
    val userValidationRequestTopic: String,

    @Value("\${topics.validation.user.result}")
    val userValidationResultTopic: String,

    val kafkaProperties: KafkaProperties,
)