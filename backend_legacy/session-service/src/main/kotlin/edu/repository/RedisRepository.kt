package edu.repository

import com.fasterxml.jackson.core.JsonProcessingException
import edu.config.RedisConfig
import edu.location.sharing.models.events.connections.RemoveConnectionEvent
import edu.location.sharing.models.events.connections.StoreConnectionEvent
import edu.location.sharing.util.logger
import edu.util.objectMapper
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.time.Duration

@Repository
class RedisRepository(
    val redisConfig: RedisConfig,
    val redisTemplate: ReactiveStringRedisTemplate,
) {

    private val log = logger()

    private val ttlRetries = 2L

    private val connectionKeyPrefix = "connection:"
    private val connectionGroupKeyPrefix = "group:"

    private val connectionTtlSink = Sinks.many().unicast().onBackpressureBuffer<String>()
    private val connectionGroupTtlSink = Sinks.many().unicast().onBackpressureBuffer<String>()

    init {
        // consume events from the TTL sinks
        connectionTtlSink.asFlux()
            .bufferTimeout(100, Duration.ofSeconds(20))
            .flatMap { Flux.fromStream(it.stream().distinct()) }
            .flatMap { setConnectionTtl(it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        connectionGroupTtlSink.asFlux()
            .bufferTimeout(100, Duration.ofSeconds(20))
            .flatMap { Flux.fromStream(it.stream().distinct()) }
            .flatMap { setConnectionGroupTtl(it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    fun storeConnection(event: StoreConnectionEvent): Mono<Void> {
        return addConnection(event.connectionId, event)
            .then(setConnectionTtl(event.connectionId))
            .then(addConnectionToGroup(event.connectionId, event.groupId))
            .then(setConnectionGroupTtl(event.groupId))
            .then()
    }

    private fun addConnection(connectionId: String, event: StoreConnectionEvent): Mono<Boolean> {
        return redisTemplate.opsForValue()
            .set(connectionKeyPrefix + connectionId, objectMapper.writeValueAsString(event))
            .doOnNext { log.info("cached connection $event") }
            .doOnError { log.error("error while storing connection $event", it) }
    }
    private fun addConnectionToGroup(connectionId: String, groupId: String): Mono<Long> {
        return redisTemplate.opsForSet()
            .add(connectionGroupKeyPrefix + groupId, connectionId)
            .doOnNext { log.info("added connection $connectionId to group $groupId") }
            .doOnError { log.error("error while storing connection $connectionId in group $groupId", it) }
    }

    private fun sinkConnectionTtl(connectionId: String) {
        connectionTtlSink.emitNext(connectionId) { _, emitResult ->
            if (emitResult.isFailure) {
                log.warn("failed to put connectionId $connectionId into sink")
            }
            false
        }
    }

    private fun sinkConnectionGroupTtl(connectionGroupId: String) {
        connectionGroupTtlSink.emitNext(connectionGroupId) { _, emitResult ->
            if (emitResult.isFailure) {
                log.warn("failed to put connectionGroupId $connectionGroupId into sink")
            }
            false
        }
    }

    private fun setConnectionTtl(connectionId: String): Mono<Boolean> {
        return setTtl(connectionKeyPrefix + connectionId, redisConfig.connectionTtl)
    }

    private fun setConnectionGroupTtl(groupId: String): Mono<Boolean> {
        return setTtl(connectionGroupKeyPrefix + groupId, redisConfig.connectionGroupTtl)
    }

    private fun setTtl(key: String, ttl: Duration): Mono<Boolean> {
        return redisTemplate
            .expire(key, ttl)
            .doOnNext { log.info("set TTL $ttl on key $key") }
            .doOnError { log.warn("failed to set TTL on key $key") }
            .retry(ttlRetries)
    }

    fun removeConnection(event: RemoveConnectionEvent): Mono<Boolean> {
        return deleteConnectionFromGroup(event.connectionId, event.groupId)
            .then(deleteConnection(event.connectionId))
    }

    private fun deleteConnectionFromGroup(connectionId: String, groupId: String): Mono<Long> {
        return redisTemplate.opsForSet()
            .remove(connectionGroupKeyPrefix + groupId, connectionId)
            .doOnNext { log.info("deleted connection $connectionId from group $groupId") }
            .doOnError { log.error("error while deleting connection $connectionId from group $groupId", it) }
    }

    private fun deleteConnection(connectionId: String): Mono<Boolean> {
        return redisTemplate.opsForValue()
            .delete(connectionKeyPrefix + connectionId)
            .doOnNext { log.info("deleted connection $connectionId") }
            .doOnError { log.error("error while deleting connection $connectionId", it) }
    }

    fun getGroupConnections(groupId: String): Flux<StoreConnectionEvent> {
        return fetchGroupConnections(groupId)
            .flatMap { fetchConnection(it) }
    }

    private fun fetchGroupConnections(groupId: String): Flux<String> {
        return redisTemplate.opsForSet()
            .members(connectionGroupKeyPrefix + groupId)
            .doOnNext { log.info("retrieved connection group connection $it") }
            .doOnError { log.error("error while fetching group connection for group $groupId") }
            .doOnNext { sinkConnectionGroupTtl(groupId) }
    }

    private fun fetchConnection(connectionId: String): Mono<StoreConnectionEvent> {
        // TODO: optimization: maybe get multiple keys with mget
        return redisTemplate.opsForValue()
            .get(connectionKeyPrefix + connectionId)
            .doOnNext { log.info("retrieved connection $it") }
            .doOnError { log.error("error while fetching connection $connectionId") }
            .doOnNext { sinkConnectionTtl(connectionId) }
            .map { objectMapper.readValue(it, StoreConnectionEvent::class.java) }
            .doOnError(JsonProcessingException::class.java) {
                log.error("error while converting String to StoreConnectionEvent", it)
            }
    }
}