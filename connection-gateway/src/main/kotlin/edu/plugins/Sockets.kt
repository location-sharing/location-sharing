package edu.plugins

import edu.config.KafkaConfig
import edu.connections.Connection
import edu.connections.ConnectionStore
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.models.Message
import edu.service.ConnectionEventSender
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*

private val LOG = KtorSimpleLogger("WebSocketHandler")

fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {

        webSocket("/location") {

            // TODO: verify JWT

            // for now use the first frame to set the connection
            val firstFrame = incoming.receive()
            var message: Message

            if (firstFrame !is Frame.Text) {
                LOG.warn("incoming frame is not text, ignoring it")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "only text frames are accepted"))
                return@webSocket
            }

            try {
                message = parseMessage(firstFrame)
            } catch (e: Exception) {
                LOG.warn("parse exception at frame: $firstFrame; {}", e)
                close(CloseReason(
                    CloseReason.Codes.INTERNAL_ERROR, "frame parse error, probably invalid message format")
                )
                return@webSocket
            }

            val connectionId: UUID = ConnectionStore.put(message.userId, Connection(this))
            LOG.info("user ${message.userId} connected, connection stored with id $connectionId")

            ConnectionEventSender.sendStoreConnectionEvent(
                StoreConnectionEvent(message.userId, connectionId.toString(), KafkaConfig.receiveTopicName)
            )

            try {
                for (frame in incoming) {
                    if (frame !is Frame.Text) {
                        LOG.warn("incoming frame is not text, ignoring it")
                        continue
                    }

                    try {
                        message = parseMessage(frame)
                    } catch (e: Exception) {
                        LOG.warn("parse exception at frame: $firstFrame; {}", e)
                        continue
                    }

                    LOG.info("received message from user: ${message.userId}")
                }
            } catch (e: ClosedReceiveChannelException) {
                // connection closed successfully
                LOG.info("connection with id $connectionId closed")
            } catch (e: Exception) {
                // connection closed with an exception
                LOG.warn("connection with id $connectionId closed with exception: $e")
            } finally {
                ConnectionEventSender.sendRemoveConnectionEvent(
                    RemoveConnectionEvent(message.userId, connectionId.toString())
                )
                ConnectionStore.removeConnection(connectionId)
                LOG.info("removed connection with id $connectionId")
            }
        }
    }
}

private fun parseMessage(frame: Frame.Text): Message {
    return Json.decodeFromString(frame.readText())
}

