package edu.security.jwt

import edu.util.objectMapper
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFailureHandler : ServerAuthenticationFailureHandler{

    data class AuthException(
        val title: String = "Authentication failed",
        val detail: String
    )

    override fun onAuthenticationFailure(filterExchange: WebFilterExchange?, ex: AuthenticationException?): Mono<Void> {

        if (filterExchange == null || ex == null) {
            return Mono.empty()
        }

        val exchange = filterExchange.exchange

        val authException = AuthException(
            detail = ex.message ?: "Invalid authentication token or malformed Authorization header found"
        )

        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON

        val bytes = objectMapper.writeValueAsBytes(authException)
        val dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(bytes)

        return exchange.response.writeWith(Mono.just(dataBuffer))
    }
}