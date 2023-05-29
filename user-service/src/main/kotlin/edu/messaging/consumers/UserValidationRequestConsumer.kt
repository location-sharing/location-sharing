package edu.messaging.consumers

import edu.messaging.config.KafkaConfig
import edu.messaging.events.UserEvent
import edu.messaging.events.UserValidationRequestEvent
import edu.messaging.events.UserValidationResponseEvent
import edu.messaging.producers.UserValidationResponseProducer
import edu.service.ResourceNotFoundException
import edu.service.UserService
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class UserValidationRequestConsumer(
    kafkaConfig: KafkaConfig,
    private val userService: UserService,
    private val userValidationResponseProducer: UserValidationResponseProducer,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.userValidationRequestTopic
) {

    override val log = logger()

    init {
        super.createFlux().subscribe()
    }

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), UserValidationRequestEvent::class.java) }
            .doOnError { log.warn("error while parsing UserValidationRequestEvent: $it") }
            .flatMap {
                mono {
                    try {
                        val user = userService.findById(it.resourceId)

                        // send a positive validation response
                        val userEvent = UserEvent(user.id, user.username)
                        val response = UserValidationResponseEvent(
                            resourceId = it.resourceId,
                            metadata = it.metadata,
                            valid = true,
                            message = null,
                            userEvent
                        )
                        userValidationResponseProducer.sendWithResultLogging(response)
                    } catch (e: ResourceNotFoundException) {

                        // send a negative validation response
                        val response = UserValidationResponseEvent(
                            resourceId = it.resourceId,
                            metadata = it.metadata,
                            valid = false,
                            message = "User with id ${it.resourceId} not found",
                            null
                        )
                        userValidationResponseProducer.sendWithResultLogging(response)
                    }
                }
            }
    }
}