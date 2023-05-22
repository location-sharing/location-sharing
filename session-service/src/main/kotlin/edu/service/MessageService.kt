package edu.service

import edu.api.producers.ClientMessageProducer
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.util.logger
import edu.repository.RedisRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.sender.SenderResult

@Component
class MessageService(
    private val redisRepository: RedisRepository,
    private val clientMessageProducer: ClientMessageProducer,
) {
    private val log = logger()

    // TODO: use DTOs
    fun sendMessageInGroup(event: ClientMessageEvent): Flux<SenderResult<ClientMessageEvent>> {
        return redisRepository.getGroupConnections(event.groupId)
            .flatMap {
                // create a client message, using the event's content, but using the stored connection
                val clientMessageEvent = ClientMessageEvent(
                    event.userId,
                    it.groupId,
                    it.connectionId,
                    event.content
                )
                clientMessageProducer.sendWithResultLogging(clientMessageEvent)
            }
    }
}