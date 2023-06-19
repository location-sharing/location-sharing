package edu.locationsharing.controller

import edu.locationsharing.models.UserNotification
import edu.locationsharing.security.jwt.AuthenticatedUser
import edu.locationsharing.service.NotificationSink
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/notifications")
class NotificationController {

    fun subscribe(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): Flux<ServerSentEvent<UserNotification>> {
        return NotificationSink
            .filter(user)
            .map { notification ->
                ServerSentEvent.builder<UserNotification>()
                    .event("UserNotification")
                    .data(notification)
                    .build()
            }
    }
}