package edu.security

import edu.security.filters.JwtAuthenticationConverter
import edu.security.filters.JwtAuthenticationFailureHandler
import edu.security.filters.JwtAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
class SecurityConfig(
    val jwtAuthManager: JwtAuthenticationManager,
    val jwtAuthConverter: JwtAuthenticationConverter,
    val jwtAuthFailureHandler: JwtAuthenticationFailureHandler,
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        // this filter extracts the Authorization header,
        // and tries to authenticate the user with the JWT in it
        val jwtAuthFilter = AuthenticationWebFilter(jwtAuthManager)
        jwtAuthFilter.setServerAuthenticationConverter(jwtAuthConverter)
        jwtAuthFilter.setAuthenticationFailureHandler(jwtAuthFailureHandler)

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeExchange {
                it.anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}