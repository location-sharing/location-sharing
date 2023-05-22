package edu.plugins

import edu.models.Message
import edu.service.ConnectionService.removeConnection
import edu.service.ConnectionService.sendRemoveConnectionEvent
import edu.service.ConnectionService.sendStoreConnectionEvent
import edu.service.ConnectionService.storeConnection
import edu.service.MessageEventService.sendClientMessageEvent
import edu.util.objectMapper
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
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

            // TODO: verify JWT, use it to send StoreConnectionEvent?

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

            val connectionId = storeConnection(this)
            LOG.info("user ${firstMessage.userId} connected, connection stored with id $connectionId")

            sendStoreConnectionEvent(connectionId, firstMessage)
            sendClientMessageEvent(connectionId, firstMessage)

            // TODO: send response after connection has been stored by the session service?

            try {
               processMessages(connectionId)
            } catch (e: ClosedReceiveChannelException) {
                LOG.info("connection with id $connectionId closed")
            } catch (e: Exception) {
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
    return objectMapper.readValue(frame.readText(), Message::class.java)
}

class InvalidFrameFormatException: Exception()

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

private suspend fun WebSocketSession.processMessages(connectionId: UUID) {
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
        sendClientMessageEvent(connectionId, message)
    }
}
