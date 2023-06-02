package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.util.logger
import edu.models.GroupEvent
import edu.service.ClientMessageSender
import java.time.Duration.ofMillis
import java.util.*

object GroupEventConsumer: GenericConsumer(
    KafkaConfig.groupEventsTopic,
    pollTimeout = ofMillis(300000),
    mapOf(
        "group.id" to "group.event.consumer.${UUID.randomUUID()}"
    )
) {
    override val log = logger()

    override suspend fun process(event: GroupEvent) {
        ClientMessageSender.sendToGroup(event)
    }
}