package edu.plugins

import edu.security.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureJwtAuth() {

    install(Authentication) {
        jwt(JwtConfig.JWT_AUTH_NAME) {
            realm = JwtConfig.REALM
            verifier(JwtConfig.VERIFIER)
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is invalid or has expired.")
            }
        }
    }
}