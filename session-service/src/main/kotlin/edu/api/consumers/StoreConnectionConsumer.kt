package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import edu.util.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class StoreConnectionConsumer(
    kafkaConfig: KafkaConfig,
    private val connectionService: ConnectionService
): GenericConsumer<String, StoreConnectionEvent>(
    kafkaConfig,
    kafkaConfig.storeConnectionTopic,
    { _, data -> objectMapper.readValue(data, StoreConnectionEvent::class.java) }
) {
    override val log = logger()

    override fun processFlux(flux: Flux<StoreConnectionEvent>): Flux<Any> {
        return flux.flatMap { connectionService.cacheConnection(it) }
    }
}