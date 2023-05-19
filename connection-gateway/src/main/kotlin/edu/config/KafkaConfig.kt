package edu.config

import edu.location.sharing.util.logger
import io.ktor.server.config.*
import java.util.*

object KafkaConfig {
    private val log = logger()

    private val config = ApplicationConfig("application.yaml")
    val kafkaOptions: Map<String, Any?> = config.config("kafka").toMap()

    val storeConnectionTopic = config.property("kafka_topics.session_store_connection_topic").getString()
    val removeConnectionTopic = config.property("kafka_topics.session_remove_connection_topic").getString()
    val clientMessageOutboundTopic = config.property("kafka_topics.session_client_message_topic").getString()
    val clientMessageInboundTopic = config.property("kafka_topics.gateway_client_message_topic").getString()

    // TODO: implement selective routing of messages (so we don't check every time for every message if we contain the correct connection)
    // create a random topic every time the program starts up, send it in the StoreConnectionEvent

    init {
        log.debug("loaded kafka configs file")
        log.debug("$kafkaOptions")
        log.debug("store connection topic: $storeConnectionTopic")
        log.debug("remove connection topic: $removeConnectionTopic")
        log.debug("client message topic: $clientMessageOutboundTopic")
    }
}
