package edu.models

import io.ktor.websocket.*

data class Connection(
    val session: WebSocketSession
)

