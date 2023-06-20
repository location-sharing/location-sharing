package edu.messaging.consumers

import edu.dto.user.UserDto
import edu.location.sharing.events.validation.user.UserEvent
import edu.location.sharing.events.validation.user.UserValidationRequestEvent
import edu.location.sharing.events.validation.user.UserValidationResultEvent
import edu.messaging.config.KafkaConfig
import edu.messaging.producers.UserValidationResultProducer
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
    private val userValidationResultProducer: UserValidationResultProducer,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.userValidationRequestTopic
) {

    override val log = logger()

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), UserValidationRequestEvent::class.java) }
            .doOnError { log.warn("error while parsing UserValidationRequestEvent: $it") }
            .flatMap {
                mono {
                    try {
                        val user: UserDto = if (it.userId != null) {
                            userService.findById(it.userId!!)
                        } else if (it.username != null) {
                            userService.findByUsername(it.username!!)
                        } else {
                            // invalid request, both userId and username are null
                            sendNegativeResponse(it, "Invalid validation request")
                            return@mono
                        }
                        val userEvent = UserEvent(user.id, user.username)
                        sendPositiveResponse(it, userEvent)
                    } catch (e: ResourceNotFoundException) {
                        sendNegativeResponse(it, "User not found")
                    } catch (e: Exception) {
                        sendNegativeResponse(it, "Unknown error occurred")
                    }
                }
            }
    }

    private suspend fun sendNegativeResponse(
        request: UserValidationRequestEvent,
        errorMessage: String,
    ) {
        val response = UserValidationResultEvent(
            metadata = request.metadata,
            valid = false,
            message = errorMessage,
            null
        )
        userValidationResultProducer.sendWithResultLogging(response)
    }

    private suspend fun sendPositiveResponse(
        request: UserValidationRequestEvent,
        userEvent: UserEvent,
    ) {
        val response = UserValidationResultEvent(
            metadata = request.metadata,
            valid = true,
            message = null,
            userEvent
        )
        userValidationResultProducer.sendWithResultLogging(response)
    }
}