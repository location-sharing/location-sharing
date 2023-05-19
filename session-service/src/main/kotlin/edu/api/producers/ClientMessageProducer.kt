package edu.api.producers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.springframework.stereotype.Component

@Component
class ClientMessageProducer(
    kafkaConfig: KafkaConfig
): GenericReactiveProducer<String, ClientMessageEvent>(
    kafkaConfig,
    { _, data ->  objectMapper.writeValueAsBytes(data) }
) {
    override val log = logger()
}