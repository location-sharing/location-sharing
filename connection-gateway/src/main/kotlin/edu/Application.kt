package edu

import edu.api.consumers.startConsumers
import edu.plugins.configureJwtAuth
import edu.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureJwtAuth()
    configureSockets()
    startConsumers()
}
