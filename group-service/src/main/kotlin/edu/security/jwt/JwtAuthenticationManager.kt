package edu.security.jwt

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Attempts to authenticate a user (verify and re-populate the Authentication object)
 */
@Component
class JwtAuthenticationManager(
    private val jwtUtils: JwtUtils
): ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {

        if (authentication == null) {
            return Mono.empty()
        }

        val jwt: DecodedJWT
        try {
            jwt = jwtUtils.verify(authentication.name)
        } catch (e: TokenExpiredException) {
            throw BadCredentialsException("${e.message}")
        } catch (e: JWTVerificationException) {
            throw BadCredentialsException("Invalid token")
        }

        // TODO: set roles and permissions

        val authenticatedUser = jwtUtils.toAuthenticatedUser(jwt)
        return Mono.just(
            UsernamePasswordAuthenticationToken(authenticatedUser, jwt.token, listOf())
        )
    }
}