package edu.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import java.time.Duration

@Configuration
class RedisConfig(
    @Value("\${cache.connection_group_ttl:PT5M}")
    val connectionGroupTtl: Duration,

    @Value("\${cache.connection_ttl:PT5M}")
    val connectionTtl: Duration,
) {
    @Bean
    fun reactiveStringRedisTemplate(connectionFactory: ReactiveRedisConnectionFactory):
            ReactiveStringRedisTemplate = ReactiveStringRedisTemplate(connectionFactory)
}