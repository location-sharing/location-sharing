package edu.security

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Extracts authentication credentials (JWT) from a request
 */
@Component
class JwtAuthenticationConverter(
    private val jwtUtils: JwtUtils
): ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> {

        if (exchange == null) {
            return Mono.empty()
        }

        val authHeader = exchange.request.headers[HttpHeaders.AUTHORIZATION]
        if (authHeader.isNullOrEmpty()) {
            return Mono.empty()
        }

        if (authHeader.size != 1) {
            throw AuthenticationCredentialsNotFoundException("Malformed Authorization Bearer token header")
        }

        val bearerTokenList = authHeader[0].split(" ")
        if (bearerTokenList.size != 2) {
            throw AuthenticationCredentialsNotFoundException("Malformed Authorization Bearer token header")
        }

        val token = bearerTokenList[1]

//        val jwt: DecodedJWT
//        try {
//            jwt = jwtUtils.verify(token)
//        } catch (e: JWTVerificationException) {
//            throw BadCredentialsException("Invalid token")
//        }
//        val authenticatedUser = jwtUtils.toAuthenticatedUser(jwt)

        return Mono.just(
            UsernamePasswordAuthenticationToken(token, token)
        )
    }
}