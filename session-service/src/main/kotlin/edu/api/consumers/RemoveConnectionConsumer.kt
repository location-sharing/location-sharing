package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import edu.util.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class RemoveConnectionConsumer(
    kafkaConfig: KafkaConfig,
    private val connectionService: ConnectionService
): GenericConsumer<String, RemoveConnectionEvent>(
    kafkaConfig,
    kafkaConfig.removeConnectionTopic,
    { _, data -> objectMapper.readValue(data, RemoveConnectionEvent::class.java) }
) {

    override val log = logger()

    override fun processFlux(flux: Flux<RemoveConnectionEvent>): Flux<Any> {
        return flux.flatMap { connectionService.removeConnection(it)}
    }
}