package edu.plugins

import edu.config.KafkaConfig
import edu.connections.Connection
import edu.connections.ConnectionStore
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import edu.models.Message
import edu.service.ClientMessageSender
import edu.service.ConnectionEventSender
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

private val LOG = LoggerFactory.getLogger("WebSocketLogger")

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

            val firstMessage: Message
            try {
                firstMessage = parseFirstMessage()
            } catch (e: InvalidFrameFormatException) {
                close(CloseReason(
                    CloseReason.Codes.INTERNAL_ERROR, "frame parse error, probably invalid message format")
                )
                return@webSocket
            }

            val connectionId = storeThisConnection()
            LOG.info("user ${firstMessage.userId} connected, connection stored with id $connectionId")

            sendStoreConnectionEvent(connectionId, firstMessage)
            sendClientMessage(firstMessage)

            // TODO: send response after connection has been stored by the session service?

            try {
               processMessages()
            } catch (e: ClosedReceiveChannelException) {
                // connection closed successfully
                LOG.info("connection with id $connectionId closed")
            } catch (e: Exception) {
                // connection closed with an exception
                LOG.warn("connection with id $connectionId closed with exception: $e")
            } finally {
                sendRemoveConnectionEvent(connectionId, firstMessage)
                removeConnection(connectionId)
                LOG.info("removed connection with id $connectionId")
            }
        }
    }
}

private fun parseMessage(frame: Frame.Text): Message {
    return Json.decodeFromString(frame.readText())
}

class InvalidFrameFormatException(
//    override val message: String? = null
): Exception()

private suspend inline fun WebSocketSession.parseFirstMessage(): Message {
    val firstFrame = incoming.receive()

    if (firstFrame !is Frame.Text) {
        LOG.warn("incoming frame is not text, ignoring it")
        throw InvalidFrameFormatException()
    }

    try {
        return parseMessage(firstFrame)
    } catch (e: Exception) {
        LOG.warn("parse exception at frame: $firstFrame; {}", e)
        throw InvalidFrameFormatException()
    }
}

private fun WebSocketSession.storeThisConnection(): UUID = ConnectionStore.put(Connection(this))
private fun removeConnection(connectionId: UUID) = ConnectionStore.remove(connectionId)

private fun sendStoreConnectionEvent(connectionId: UUID, message: Message) {
    ConnectionEventSender.sendStoreConnectionEvent(
        StoreConnectionEvent(
            message.userId,
            message.groupId,
            connectionId.toString(),
            KafkaConfig.receiveTopic
        )
    )
}

private fun sendRemoveConnectionEvent(connectionId: UUID, message: Message) {
    ConnectionEventSender.sendRemoveConnectionEvent(
        RemoveConnectionEvent(
            message.userId,
            message.groupId,
            connectionId.toString(),
            KafkaConfig.receiveTopic
        )
    )
}

private fun sendClientMessage(message: Message) = ClientMessageSender.sendMessage(message)

private suspend fun WebSocketSession.processMessages() {
    for (frame in incoming) {
        if (frame !is Frame.Text) {
            LOG.warn("incoming frame is not text, ignoring it")
            continue
        }

        val message = try {
            parseMessage(frame)
        }
        catch (e: Exception) {
            LOG.warn("parse exception at frame: $frame; {}", e)
            continue
        }

        LOG.info("received message from user: ${message.userId}")
    }
}
