package edu.messaging.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(

    @Value("\${topics.validation.user.request}")
    val userValidationRequestTopic: String,

    @Value("\${topics.validation.user.result}")
    val userValidationResponseTopic: String,

    @Value("\${topics.validation.group.user.request}")
    val groupUserValidationRequestTopic: String,

    @Value("\${topics.validation.group.user.result}")
    val groupUserValidationResultTopic: String,

    @Value("\${topics.notifications.system}")
    val systemNotificationsTopic: String,

    @Value("\${topics.notifications.user}")
    val userNotificationsTopic: String,

    val kafkaProperties: KafkaProperties,
)