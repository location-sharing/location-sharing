package edu.api.consumers

import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.util.logger
import edu.service.ConnectionService
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Component
class RemoveConnectionConsumer(
    private val consumer: ReactiveKafkaConsumerTemplate<String, RemoveConnectionEvent>,
    private val connectionService: ConnectionService
) {

    private val log = logger()

    fun createFlux(): Flux<Any> {
        log.info("creating event flux in ${this::class.qualifiedName}")
        return consumer
            .receiveAutoAck()
            .publishOn(Schedulers.parallel())
            .doOnNext { log.info("received record $it") }
            .map { it.value() }
            .flatMap { connectionService.removeConnection(it) }
    }
}