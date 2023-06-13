package edu.plugins

import edu.config.CorsConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) { CorsConfig.installCors(this) }
}