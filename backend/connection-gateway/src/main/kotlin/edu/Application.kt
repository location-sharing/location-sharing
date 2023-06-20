package edu

import edu.messaging.consumers.startConsumers
import edu.plugins.configureCors
import edu.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureSockets()
    startConsumers()
}
