package edu.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import edu.location.sharing.util.logger
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

object JwtUtils {
    private val jwtOptions = ApplicationConfig("application.yaml").config("jwt").toMap()

    private val SECRET = jwtOptions["secret"] as String
    private val ISSUER = jwtOptions["issuer"] as String
    private val AUDIENCE = jwtOptions["audience"] as String

    private const val CLAIM_USER_ID = "userId"
    private const val CLAIM_USER_NAME = "username"

    private val VERIFIER: JWTVerifier by lazy {
        JWT.require(Algorithm.HMAC384(SECRET))
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaimPresence(CLAIM_USER_ID)
            .withClaimPresence(CLAIM_USER_NAME)
            .build()
    }

    fun verify(jwt: String): DecodedJWT {
        return VERIFIER.verify(jwt)
    }

    fun toAuthenticatedUser(jwt: DecodedJWT): AuthenticatedUser {
        val userId = jwt.getClaim(CLAIM_USER_ID).asString()
        val username = jwt.getClaim(CLAIM_USER_NAME).asString()
        return AuthenticatedUser(
            userId, username
        )
    }

    /**
     * For the ktor authentication plugin
     */
    fun JWTPrincipal.toAuthenticatedUser(): AuthenticatedUser {
        val userId = payload.getClaim(CLAIM_USER_ID)
        val username = payload.getClaim(CLAIM_USER_NAME)
        if (userId.isMissing || username.isMissing ) {
            throw AuthenticationException(
                detail = "Token doesn't contain $CLAIM_USER_ID and $CLAIM_USER_NAME claims"
            )
        }

        return AuthenticatedUser(userId.asString(), username.asString())
    }
}