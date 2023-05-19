package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import edu.util.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class ClientMessageConsumer(
    kafkaConfig: KafkaConfig,
    private val connectionService: ConnectionService,
): GenericConsumer<String, ClientMessageEvent>(
    kafkaConfig,
    kafkaConfig.clientMessagesInboundTopic,
    { _, data -> objectMapper.readValue(data, ClientMessageEvent::class.java)}
){
    override val log = logger()

    override fun processFlux(flux: Flux<ClientMessageEvent>): Flux<Any> {
        return flux.flatMap { connectionService.sendMessageInGroup(it) }
    }
}