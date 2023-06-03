package edu.plugins

import edu.models.ClientMessage
import edu.security.AuthenticatedUser
import edu.security.JwtConfig
import edu.security.toAuthenticatedUser
import edu.service.ConnectionService.userGroupConnectionsEmpty
import edu.service.ConnectionService.removeConnection
import edu.service.ConnectionService.storeConnection
import edu.service.GroupEventService.sendConnectedNotificationEvent
import edu.service.GroupEventService.sendDisconnectedNotificationEvent
import edu.service.GroupEventService.sendMessageEvent
import edu.util.objectMapper
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

private val LOG = LoggerFactory.getLogger("WebSocketLogger")

fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {

        authenticate(JwtConfig.JWT_AUTH_NAME) {

            // TODO: block further execution until a response comes back which validates that the connection has been stored
            // TODO: when a validation response comes for the specified user and group, release the latch and enable message flow

            webSocket("/group") {

                val user = call.principal<JWTPrincipal>()!!.toAuthenticatedUser()

                // TODO: we could send a notification event saying that the user is online


                // for now use the first frame to set the connection
                // don't catch an InvalidMessageException,
                // because the groupId, userId can be fetched from any message type
                val firstMessage: ClientMessage
                try {
                    firstMessage = parseFirstMessage()
                } catch (e: InvalidFrameFormatException) {
                    close(CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR, "frame parse error, invalid message format")
                    )
                    return@webSocket
                }

                val groupId = firstMessage.groupId
                val userId = user.id


                val connectionValid = AtomicBoolean(!userGroupConnectionsEmpty(groupId, userId))

                if (!connectionValid.get()) {
                    // TODO: send out a validation event, set a callback on validation success/failure

                    launch {
                        delay(20000)
                        connectionValid.set(true)
                        outgoing.send(Frame.Text("validation success"))
                    }
                }

                // lock until connection gets validated, set a timeout too
                try {
                    withTimeout(30000) {
                        while (!connectionValid.get()) {
                            delay(1000)
                            outgoing.send(Frame.Text("pending validation"))
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    close(
                        CloseReason(CloseReason.Codes.INTERNAL_ERROR, "validation failed")
                    )
                }



                storeConnection(
                    groupId,
                    userId,
                    this
                )

                LOG.info("user $userId connected to group $groupId, connection $this stored")

                sendConnectedNotificationEvent(groupId, userId)
                sendMessageEvent(groupId, userId, firstMessage.content)

                // TODO: send response after connection has been stored by the session service?

                try {
                    processMessages(user)
                } catch (e: ClosedReceiveChannelException) {
                    LOG.info("connection $this closed")
                } catch (e: Exception) {
                    LOG.warn("connection $this closed with exception: $e")
                } finally {
                    removeConnection(
                        groupId,
                        userId,
                        this
                    )
                    sendDisconnectedNotificationEvent(groupId, userId)
                    LOG.info("removed connection $this")
                }
            }
        }
    }
}

private fun parseMessage(frame: Frame.Text): ClientMessage {
    return objectMapper.readValue(frame.readText(), ClientMessage::class.java)
}

class InvalidFrameFormatException: Exception()

private suspend inline fun WebSocketSession.parseFirstMessage(): ClientMessage {
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

private suspend fun WebSocketSession.processMessages(authenticatedUser: AuthenticatedUser) {
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

        LOG.info("received message from user: ${authenticatedUser.id}")
        sendMessageEvent(message.groupId, authenticatedUser.id, message.content)
    }
}
