package edu.plugins

import com.auth0.jwt.exceptions.JWTVerificationException
import edu.models.ClientMessage
import edu.repository.ConnectionStore
import edu.security.AuthenticatedUser
import edu.security.JwtUtils
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

        get("/active") {
            val userToken = call.request.queryParameters["auth"]
            if (userToken == null) {
                LOG.info("closing connection, 'auth' request param not present")
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            try {
                JwtUtils.verify(userToken)
            } catch (e: JWTVerificationException) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val groupId = call.request.queryParameters["groupId"]
            if (groupId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val groupUsersOnline = ConnectionStore.getGroupUsers(groupId)
            call.respond(HttpStatusCode.OK, objectMapper.writeValueAsString(groupUsersOnline))
        }

        webSocket("/test") {
            LOG.info("TEST: got websocket request")

            val username = call.request.queryParameters["username"]
            val userId = call.request.queryParameters["userId"]
            if (username == null || userId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "username or userId query param not present"))
                return@webSocket
            }

            val user = AuthenticatedUser(userId, username)

            // validate the group
            val groupId = verifyGroup() ?: return@webSocket

            if (!userGroupConnectionsEmpty(groupId, user)) {
                // group membership already validated, but this particular connection has not been added
                storeConnection(groupId, user, this)
                LOG.info("TEST: received message from user: ${user.id}")
            } else {
                LOG.info("TEST: group membership validation skipped, storing connection")
                storeConnection(groupId, user, this@webSocket)

                sendConnectedNotificationEvent(groupId, user)
                LOG.info("user ${user.id} connected to group $groupId, connection ${this@webSocket} stored")
            }

            try {
                for (frame in incoming) {
                    val message = parseMessage(frame) ?: continue
                    LOG.info("TEST: parsed message $message")
                    sendMessageEvent(groupId, user, message.payload)
                }
            } catch (e: ClosedReceiveChannelException) {
                LOG.info("connection $this closed")
            } catch (e: Exception) {
                LOG.warn("connection $this closed with exception: $e")
            } finally {
                if (!userGroupConnectionsEmpty(groupId, user)) {
                    if (isTheOnlyConnection(groupId, user, this)) {
                        sendDisconnectedNotificationEvent(groupId, user)
                    }
                    removeConnection(groupId, user, this)
                    LOG.info("removed connection $this")
                }
                LOG.info("closed connection $this")
            }
        }

        webSocket("/group") {

            LOG.info("incoming websocket request")
            val user = verifyAuthToken() ?: return@webSocket

            // TODO: we could send a notification event saying that the user is online

            // validate the group
            val groupId = verifyGroup() ?: return@webSocket

            val connectionValid = AtomicBoolean(false)
            if (!userGroupConnectionsEmpty(groupId, user)) {
                // group membership already validated, but this particular connection has not been added
                storeConnection(groupId, user, this)
                LOG.info("received message from user: ${user.id}")
                connectionValid.set(true)
            } else {
                validateGroupUserMembership(groupId, user.id) {
                    LOG.info("group membership validation success, storing connection")
                    storeConnection(groupId, user, this@webSocket)

                    sendConnectedNotificationEvent(groupId, user)
                    LOG.info("user ${user.id} connected to group $groupId, connection ${this@webSocket} stored")

                    connectionValid.set(true)
                }
            }

            try {
                for (frame in incoming) {
                    if (!connectionValid.get()) continue

                    val message = parseMessage(frame) ?: continue
                    LOG.info("parsed message $message")

                    sendMessageEvent(groupId, user, message.payload)
                }
            } catch (e: ClosedReceiveChannelException) {
                LOG.info("connection $this closed")
            } catch (e: Exception) {
                LOG.warn("connection $this closed with exception: $e")
            } finally {
                if (!userGroupConnectionsEmpty(groupId, user)) {
                    if (isTheOnlyConnection(groupId, user, this)) {
                        sendDisconnectedNotificationEvent(groupId, user)
                    }
                    removeConnection(groupId, user, this)
                    LOG.info("removed connection $this")
                }
                LOG.info("closed connection $this")
            }
        }
    }
}

private suspend fun WebSocketServerSession.verifyAuthToken(): AuthenticatedUser? {
    val userToken = call.request.queryParameters["auth"]
    if (userToken == null) {
        LOG.info("closing connection, 'auth' request param not present")
        close(CloseReason(
            CloseReason.Codes.CANNOT_ACCEPT,
            "Token not present in 'auth' query param"
        ))
        return null
    }

    val decodedJwt = try {
        JwtUtils.verify(userToken)
    } catch (e: JWTVerificationException) {
        LOG.info("closing connection, 'auth' token invalid")
        close(CloseReason(
            CloseReason.Codes.CANNOT_ACCEPT,
            "Token invalid or expired"
        ))
        return null
    }

    LOG.info("auth token valid for connection $this")

    return JwtUtils.toAuthenticatedUser(decodedJwt)
}

private suspend fun WebSocketServerSession.verifyGroup(): String? {
    val groupId = call.request.queryParameters["groupId"]
    if (groupId == null) {
        close(CloseReason(
            CloseReason.Codes.CANNOT_ACCEPT,
            "groupId request parameter cannot be null"
        ))
        return null
    }
    return groupId
}

data class WebSocketMessageInvalidException(val title: String, val message: String)

private suspend fun WebSocketServerSession.parseMessage(frame: Frame): ClientMessage? {
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

private suspend fun WebSocketServerSession.validateGroupUserMembership(
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
