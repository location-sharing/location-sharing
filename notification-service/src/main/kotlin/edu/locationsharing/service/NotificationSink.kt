package edu.locationsharing.service

import edu.locationsharing.models.SystemNotification
import reactor.core.publisher.Sinks

object NotificationSink {
    val sink = Sinks.many().multicast().directBestEffort<SystemNotification>()


}