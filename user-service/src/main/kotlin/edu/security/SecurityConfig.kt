package edu.security

import edu.security.cors.CorsConfig
import edu.security.jwt.JwtAuthenticationConverter
import edu.security.jwt.JwtAuthenticationFailureHandler
import edu.security.jwt.JwtAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

/**
 * Specifies the security filter chain
 * Note that a cors filter is automatically added by {@link edu.security.cors.CorsFilter CorsFilter}
 */
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
                it
                    // for preflight requests
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/user", "/api/user/authenticate").permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(14)
    }
}