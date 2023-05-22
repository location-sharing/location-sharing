package edu.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer

val objectMapper = jacksonObjectMapper()

val kafkaByteArraySerializer = ByteArraySerializer()
val kafkaByteArrayDeserializer = ByteArrayDeserializer()
