package edu.api.consumers

import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.headers.EventType
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import edu.service.MessageService
import edu.util.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ClientMessageConsumer(
    kafkaConfig: KafkaConfig,
    private val messageService: MessageService,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.clientMessagesInboundTopic,
){
    override val log = logger()

    override fun processFlux(flux: Flux<Pair<EventType, ByteArray>>): Flux<*> {
        return flux
            .flatMap { (eventType, data) ->
                if (EventType.CLIENT_MESSAGE == eventType) {
                    val value = objectMapper.readValue(data, ClientMessageEvent::class.java)
                    messageService.sendMessageInGroup(value)
                } else {
                    Mono.empty()
                }
            }
    }
}