package edu.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtUtils(
    @Value("\${jwt.secret}")
    private val jwtSecret: String
) {

    companion object {
        val sign: (secret: String) -> Algorithm = { secret -> Algorithm.HMAC384(secret) }

        private const val JWT_ISSUER = "location-sharing-app"
        private const val JWT_AUDIENCE = "location-sharing-app"
    }

    private val jwtVerifier: JWTVerifier by lazy {
        JWT.require(sign(jwtSecret))
            .withIssuer(JWT_ISSUER)
            .withAudience(JWT_AUDIENCE)
            .withClaimPresence(JwtClaim.USER_ID.keyName)
            .withClaimPresence(JwtClaim.USER_NAME.keyName)
            .build()
    }

    fun verify(jwt: String): DecodedJWT {
        return jwtVerifier.verify(jwt)
    }

    fun toAuthenticatedUser(jwt: DecodedJWT): AuthenticatedUser {
        val userId = jwt.getClaim(JwtClaim.USER_ID.keyName).asString()
        val username = jwt.getClaim(JwtClaim.USER_NAME.keyName).asString()
        return AuthenticatedUser(
            userId, username
        )
    }
}