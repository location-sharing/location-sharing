package edu.plugins

import edu.models.ClientMessage
import edu.models.GroupEvent
import edu.models.GroupEventType
import edu.service.ConnectionService.removeConnection
import edu.service.ConnectionService.storeConnection
import edu.service.GroupEventService.sendConnectedNotificationEvent
import edu.service.GroupEventService.sendDisconnectedNotificationEvent
import edu.service.GroupEventService.sendMessageEvent
import edu.util.objectMapper
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.slf4j.LoggerFactory
import java.time.Duration

private val LOG = LoggerFactory.getLogger("WebSocketLogger")

fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {

//        authenticate(JwtConfig.JWT_AUTH_NAME) {

            // TODO: get principal from JWT, convert it to AuthenticatedUser
            // TODO: store AuthenticatedUser somewhere
            // TODO: in websockets, send a StoreConnectionEvent on first connection
            // TODO: block further execution until a response comes back which validates that the connection has been stored
            // TODO: when a validation response comes for the specified user and group, release the latch and enable message flow

            webSocket("/location") {
//
//                val principal = call.principal<JWTPrincipal>()
//                principal.payload.getClaim()

                // TODO: verify JWT, use it to send StoreConnectionEvent?

                // for now use the first frame to set the connection
                // don't catch an InvalidMessageException,
                // because the groupId, userId can be fetched from any message type
                val firstMessage: GroupEvent
                try {
                    firstMessage = parseFirstMessage()
                } catch (e: InvalidFrameFormatException) {
                    close(CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR, "frame parse error, invalid message format")
                    )
                    return@webSocket
                }


                storeConnection(
                    firstMessage.groupId,
                    firstMessage.userId,
                    this
                )

                LOG.info("user ${firstMessage.userId} connected, connection $this stored")

                sendConnectedNotificationEvent(firstMessage.groupId, firstMessage.userId)
                sendMessageEvent(firstMessage.groupId, firstMessage.userId, firstMessage.payload)

                // TODO: send response after connection has been stored by the session service?

                try {
                    processMessages()
                } catch (e: ClosedReceiveChannelException) {
                    LOG.info("connection $this closed")
                } catch (e: Exception) {
                    LOG.warn("connection $this closed with exception: $e")
                } finally {
                    removeConnection(
                        firstMessage.groupId,
                        firstMessage.userId,
                        this
                    )
                    sendDisconnectedNotificationEvent(firstMessage.groupId, firstMessage.userId)
                    LOG.info("removed connection $this")
                }
            }
        }

//    }
}

private fun parseMessage(frame: Frame.Text): GroupEvent {
    val message = objectMapper.readValue(frame.readText(), ClientMessage::class.java)
    return GroupEvent(
        message.groupId,
        message.userId,
        GroupEventType.MESSAGE,
        message.content
    )
}

class InvalidFrameFormatException: Exception()

private suspend inline fun WebSocketSession.parseFirstMessage(): GroupEvent {
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
        sendMessageEvent(message.groupId, message.userId, message.payload)
    }
}
