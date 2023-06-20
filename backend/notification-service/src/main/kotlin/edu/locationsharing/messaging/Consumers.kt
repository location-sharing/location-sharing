package edu.locationsharing.messaging

import edu.locationsharing.models.UserNotification
import edu.locationsharing.service.NotificationSink
import edu.locationsharing.util.logger
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

@Component
class Consumers {

    val log = logger()

    @Bean
    fun userNotificationConsumer(): Function<Flux<UserNotification>, Mono<Void>> = Function {
        flux ->
        flux
            .doOnNext {
                log.info("consumed notification $it")

                val result = NotificationSink.sink.tryEmitNext(it)
                if (result.isFailure) {
                    log.warn("notification could not be sent into the sink: $result")
                }
            }
            .onErrorContinue { error, _ ->
                log.error("error while consuming notification", error)
            }
            .then()
    }
}