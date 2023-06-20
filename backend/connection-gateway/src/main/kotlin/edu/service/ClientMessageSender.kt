package edu.service

import edu.location.sharing.util.logger
import edu.models.GroupEvent
import edu.repository.ConnectionStore
import edu.util.objectMapper
import io.ktor.websocket.*

object ClientMessageSender {

    private val log = logger()

    suspend fun sendToGroup(event: GroupEvent) {
        ConnectionStore
            .getGroupConnections(event.groupId)
            .forEach { session ->
                session.send(
                    Frame.Text(
                        objectMapper.writeValueAsString(event)
                    )
                )
                log.info("sent event $event on connection $session")
            }
    }
}