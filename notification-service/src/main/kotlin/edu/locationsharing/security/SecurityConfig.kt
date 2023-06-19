package edu.locationsharing.security

import edu.locationsharing.security.cors.CorsConfig
import edu.locationsharing.security.jwt.JwtAuthenticationConverter
import edu.locationsharing.security.jwt.JwtAuthenticationFailureHandler
import edu.locationsharing.security.jwt.JwtAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
class SecurityConfig(
    val jwtAuthManager: JwtAuthenticationManager,
    val jwtAuthConverter: JwtAuthenticationConverter,
    val jwtAuthFailureHandler: JwtAuthenticationFailureHandler,
    val corsConfig: CorsConfig,
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
            // by setting cors here, we guarantee that it gets invoked relatively early in the filter chain
            .cors { it.configurationSource(corsConfig.configurationSource) }
            .authorizeExchange {
                // for preflight requests
                it.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}