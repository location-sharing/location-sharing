package edu.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@Configuration
class RedisConfig(
    // default of 6h
    @Value("\${store_connection.cache_group_ttl:21600}")
    val connectionGroupTTL: Long,
) {

    @Bean
    fun reactiveStringRedisTemplate(connectionFactory: ReactiveRedisConnectionFactory):
            ReactiveStringRedisTemplate = ReactiveStringRedisTemplate(connectionFactory)
}