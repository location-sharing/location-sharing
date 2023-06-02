package edu.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*

object JwtConfig {
    private val jwtOptions = ApplicationConfig("application.yaml").config("jwt").toMap()

    val REALM = jwtOptions["realm"] as String
    private val SECRET = jwtOptions["secret"] as String
    val ISSUER = jwtOptions["issuer"] as String
    val AUDIENCE = jwtOptions["audience"] as String

    const val CLAIM_USER_ID = "userId"
    const val CLAIM_USER_NAME = "username"

    const val JWT_AUTH_NAME = "auth-jwt"

    val VERIFIER: JWTVerifier by lazy {
        JWT.require(Algorithm.HMAC384(SECRET))
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaimPresence(CLAIM_USER_ID)
            .withClaimPresence(CLAIM_USER_NAME)
            .build()
    }
}