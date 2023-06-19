package edu.locationsharing.service

import edu.locationsharing.models.UserNotification
import edu.locationsharing.security.jwt.AuthenticatedUser
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

object NotificationSink {
    val sink = Sinks.many().multicast().directBestEffort<UserNotification>()

    fun filter(user: AuthenticatedUser): Flux<UserNotification> {
        return sink
            .asFlux()
            .filter {
                it.userId == user.id
            }
    }

}