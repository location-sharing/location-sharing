package edu.security

import io.ktor.server.auth.jwt.*

fun JWTPrincipal.toAuthenticatedUser(): AuthenticatedUser {
    val userId = payload.getClaim(JwtConfig.CLAIM_USER_ID)
    val username = payload.getClaim(JwtConfig.CLAIM_USER_NAME)
    if (userId.isMissing || username.isMissing ) {
        throw AuthenticationException(
            detail = "Token doesn't contain ${JwtConfig.CLAIM_USER_ID} and ${JwtConfig.CLAIM_USER_NAME} claims (or they are null)"
        )
    }

    return AuthenticatedUser(userId.asString(), username.asString())
}