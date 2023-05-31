package edu.messaging.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.connections.RemoveConnectionEvent
import edu.location.sharing.models.events.connections.StoreConnectionEvent
import edu.location.sharing.models.events.connections.headers.EventType
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import edu.util.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ConnectionEventConsumer(
    kafkaConfig: KafkaConfig,
    private val connectionService: ConnectionService
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.connectionEventsTopic,
) {
    override val log = logger()

    override fun processFlux(flux: Flux<Pair<EventType, ByteArray>>): Flux<*> {
        return flux
            .flatMap { (eventType, data) ->
                when(eventType) {
                    EventType.STORE_CONNECTION -> {
                        val value = objectMapper.readValue(data, StoreConnectionEvent::class.java)
                        connectionService.cacheConnection(value)
                    }
                    EventType.REMOVE_CONNECTION -> {
                        val value = objectMapper.readValue(data, RemoveConnectionEvent::class.java)
                        connectionService.removeConnection(value)
                    }
                    else -> Mono.empty()
                }
            }
    }
}