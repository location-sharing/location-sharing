package edu.plugins

import edu.models.ClientMessage
import edu.security.JwtConfig
import edu.security.toAuthenticatedUser
import edu.service.ConnectionService.isTheOnlyConnection
import edu.service.ConnectionService.removeConnection
import edu.service.ConnectionService.storeConnection
import edu.service.ConnectionService.userGroupConnectionsEmpty
import edu.service.GroupEventService.sendConnectedNotificationEvent
import edu.service.GroupEventService.sendDisconnectedNotificationEvent
import edu.service.GroupEventService.sendMessageEvent
import edu.util.objectMapper
import edu.validation.GroupUserValidationService
import edu.validation.isPending
import edu.validation.isValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
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

        get("/") {
            call.respondText { "Hello World" }
        }

//        authenticate(JwtConfig.JWT_AUTH_NAME) {

            webSocket("/group") {

                val userToken = call.request.queryParameters["auth"]
                if (userToken == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Token not present")
                }

                val user = call.principal<JWTPrincipal>()!!.toAuthenticatedUser()
                val userId = user.id

                // TODO: we could send a notification event saying that the user is online

                // validate the group
                val groupId = call.request.queryParameters["groupId"]

                if (groupId == null) {
                    close(CloseReason(
                        CloseReason.Codes.CANNOT_ACCEPT,
                        "groupId request parameter cannot be null"
                    ))
                    return@webSocket
                }

                if (!userGroupConnectionsEmpty(groupId, userId)) {
                    // group membership already validated, but this particular connection has not been added
                    storeConnection(groupId, userId, this)
                    LOG.info("received message from user: ${user.id}")
                } else {
                    validateGroupUserMembership(groupId, userId) {
                        LOG.info("group membership validation success, storing connection")
                        storeConnection(groupId, userId, this@webSocket)

                        sendConnectedNotificationEvent(groupId, userId)
                        LOG.info("user $userId connected to group $groupId, connection ${this@webSocket} stored")
                    }
                }

                try {
                    for (frame in incoming) {
                        val message = parseMessage(frame) ?: continue
                        LOG.info("parsed message $message")

                        sendMessageEvent(groupId, user.id, message.content)

                    }
                } catch (e: ClosedReceiveChannelException) {
                    LOG.info("connection $this closed")
                } catch (e: Exception) {
                    LOG.warn("connection $this closed with exception: $e")
                } finally {
                    if (!userGroupConnectionsEmpty(groupId, user.id)) {
                        removeConnection(groupId, user.id, this)
                        LOG.info("removed connection $this")

                        if (isTheOnlyConnection(groupId, user.id, this)) {
                            sendDisconnectedNotificationEvent(groupId, user.id)
                        }
                    }
                    LOG.info("closed connection $this")
                }
            }
//        }
    }
}

data class WebSocketMessageInvalidException(val title: String, val message: String)

private suspend fun WebSocketSession.parseMessage(frame: Frame): ClientMessage? {
    if (frame !is Frame.Text) {
        LOG.warn("incoming frame is not text, ignoring it")
        outgoing.send(Frame.Text(
            objectMapper.writeValueAsString(
                WebSocketMessageInvalidException("Invalid message", "Only JSON messages are supported")
            )
        ))
        return null
    }

    return try {
        objectMapper.readValue(frame.readText(), ClientMessage::class.java)
    } catch (e: Exception) {
        LOG.warn("parse exception at frame: $frame; {}", e)
        outgoing.send(Frame.Text(
            objectMapper.writeValueAsString(
                WebSocketMessageInvalidException("Invalid message", "Messages must have the right JSON format")
            )
        ))
        null
    }
}

private suspend fun WebSocketSession.validateGroupUserMembership(
    groupId: String,
    userId: String,
    onSuccess: () -> Unit
) {
    val validationState = GroupUserValidationService.sendValidationRequest(groupId, userId)
    try {
        withTimeout(30000) {
            while (validationState.isPending()) {
                delay(50)
            }
        }
    } catch (e: TimeoutCancellationException) {
        close(CloseReason(
            CloseReason.Codes.INTERNAL_ERROR,
            "Group validation timed out"
        ))
    }

    if (validationState.isValid()) {
        onSuccess()
        return
    } else {
        close(CloseReason(
            CloseReason.Codes.INTERNAL_ERROR,
            "Group validation error"
        ))
    }
}
