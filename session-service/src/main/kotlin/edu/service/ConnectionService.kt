package edu.service

import edu.api.producers.ClientMessageProducer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.repository.RedisRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult

@Service
class ConnectionService(
    private val kafkaConfig: KafkaConfig,
    private val redisRepository: RedisRepository,
    private val clientMessageProducer: ClientMessageProducer,
) {
    private val log = logger()

    // TODO: use DTOs
    fun cacheConnection(event: StoreConnectionEvent): Mono<Void> {
        return redisRepository.storeConnection(event)
            .then()
    }

    fun removeConnection(event: RemoveConnectionEvent): Mono<Boolean> {
        return redisRepository.removeConnection(event)
    }

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
                clientMessageProducer.sendWithResultLogging(kafkaConfig.clientMessagesOutboundTopic, clientMessageEvent)
            }
    }
}