package edu.security

import io.ktor.server.auth.jwt.*

fun JWTPrincipal.toAuthenticatedUser(): AuthenticatedUser {
    val userId = payload.getClaim(JwtConfig.CLAIM_USER_ID)
    val username = payload.getClaim(JwtConfig.CLAIM_USER_NAME)
    if (userId == null || username == null) {
        throw AuthenticationException(
            detail = "Token doesn't contain ${JwtConfig.CLAIM_USER_ID} and ${JwtConfig.CLAIM_USER_NAME} claims"
        )
    }

    return AuthenticatedUser(userId.asString(), username.asString())
}