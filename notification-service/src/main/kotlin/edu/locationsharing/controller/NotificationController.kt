package edu.locationsharing.controller

import edu.locationsharing.models.UserNotification
import edu.locationsharing.security.jwt.JwtUtils
import edu.locationsharing.service.NotificationSink
import edu.locationsharing.util.logger
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    val jwtUtils: JwtUtils
) {

    val log = logger()

    @GetMapping
    fun subscribe(
        @RequestParam(required = true) token: String
    ): Flux<ServerSentEvent<UserNotification>> {

        val jwt = try {
            jwtUtils.verify(token)
        } catch (e: Exception) {
            throw UnauthorizedException()
        }

        val user = jwtUtils.toAuthenticatedUser(jwt)

        log.info("user ${user.id} subscribed")

        return NotificationSink
            .filter(user)
            .doOnNext {
                log.debug("sending notification $it to user ${user.id}")
            }
            .map { notification ->
                ServerSentEvent.builder<UserNotification>()
                    .event("UserNotification")
                    .data(notification)
                    .build()
            }
    }
}