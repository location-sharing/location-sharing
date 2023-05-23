package edu.service

import edu.api.producers.ClientMessageProducer
import edu.config.KafkaConfig
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.repository.RedisRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ConnectionService(
    private val redisRepository: RedisRepository,
) {
    private val log = logger()

    // TODO: use DTOs
    fun cacheConnection(event: StoreConnectionEvent): Mono<Void> {
        return redisRepository.storeConnection(event)
    }

    fun removeConnection(event: RemoveConnectionEvent): Mono<Boolean> {
        return redisRepository.removeConnection(event)
    }
}