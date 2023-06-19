package edu.locationsharing.security

import edu.locationsharing.security.cors.CorsConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class SecurityConfig(
    val corsConfig: CorsConfig,
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        // only here for cors...

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            // by setting cors here, we guarantee that it gets invoked relatively early in the filter chain
            .cors { it.configurationSource(corsConfig.configurationSource) }
            .authorizeExchange {
                it.anyExchange().permitAll()
            }
            .build()
    }
}