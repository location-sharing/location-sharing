package edu.locationsharing.controller

import edu.locationsharing.models.SystemNotification
import edu.locationsharing.security.jwt.AuthenticatedUser
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/notifications")
class NotificationController(

) {

    fun subscribe(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): Flux<ServerSentEvent<SystemNotification>> {

    }
}