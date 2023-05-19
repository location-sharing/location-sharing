package edu.repository

import com.fasterxml.jackson.core.JsonProcessingException
import edu.config.RedisConfig
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class RedisRepository(
    val redisConfig: RedisConfig,
    val redisTemplate: ReactiveStringRedisTemplate,
) {

    private val log = logger()

    fun storeConnection(event: StoreConnectionEvent): Mono<Long> {
        return redisTemplate.opsForSet()
            .add(event.groupId, objectMapper.writeValueAsString(event))
            .doOnNext { log.info("cached connection $event") }
            .doOnError { log.error("error while storing connection $event", it) }
            .onErrorResume { Mono.empty() }
    }

    fun setExpirationTime(event: StoreConnectionEvent): Mono<Boolean> {
        return redisTemplate
            .expire(event.groupId, Duration.ofSeconds(redisConfig.connectionGroupTTL))
            .doOnNext { log.info("set TTL on connection group ${event.groupId}") }
            .doOnError { log.warn("failed to set TTL on connection group ${event.groupId}") }
            .onErrorResume { Mono.empty() }
    }

    fun removeConnection(event: RemoveConnectionEvent): Mono<Long> {
        return redisTemplate.opsForSet()
            .remove(event.groupId, objectMapper.writeValueAsString(event))
            .doOnNext { log.info("removed connection $event") }
            .doOnError { log.error("error while removing connection $event", it) }
    }

    fun getGroupConnections(groupId: String): Flux<StoreConnectionEvent> {
        return redisTemplate.opsForSet()
            .members(groupId)
            .doOnNext { log.info("retrieved connection group connection $it") }
            .doOnError { log.error("error while fetching group connection for group $groupId") }
            .onErrorResume { Mono.empty() }
            .map { objectMapper.readValue(it, StoreConnectionEvent::class.java) }
            .doOnError(JsonProcessingException::class.java) {
                log.error("error while converting String to StoreConnectionEvent", it)
            }
            .onErrorResume { Mono.empty() }
    }
}