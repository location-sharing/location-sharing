package edu.service

import edu.location.sharing.util.logger
import edu.models.Message
import edu.util.objectMapper
import io.ktor.websocket.*
import java.util.*

object MessageSender {

    private val logger = logger()

    suspend fun sendToConnection(connectionId: String, message: Message) {
        val connection = ConnectionStore.get(UUID.fromString(connectionId))
        connection?.session?.send(
            Frame.Text(
                objectMapper.writeValueAsString(message)
            )
        )

        logger.info("sent message $message on connection $connection")
    }
}