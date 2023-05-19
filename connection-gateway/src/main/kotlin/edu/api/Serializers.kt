package edu.api

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

/**
 * Singleton string serializer
 */
val stringSerializer = StringSerializer()
val stringDeserializer = StringDeserializer()