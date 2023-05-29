package edu.messaging.consumers

import edu.messaging.config.KafkaConfig
import edu.messaging.events.AdditionalInfoKey
import edu.messaging.events.UserValidationPurpose
import edu.messaging.events.UserValidationResponseEvent
import edu.service.GroupService
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class UserValidationResponseConsumer(
    kafkaConfig: KafkaConfig,
    private val groupService: GroupService,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.userValidationResponseTopic
) {

    override val log = logger()

    init {
        super.createFlux().subscribe()
    }

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), UserValidationResponseEvent::class.java) }
            .doOnError { log.warn("error while parsing UserValidationResponseEvent: $it") }
            .filter { it.metadata.purpose == UserValidationPurpose.GROUP_ADD_USER}
            .doOnNext {
                if (!it.valid) {
                    // TODO: send notification about the validation status
                    log.warn("pending request for user ${it.user} invalid: ${it.message}")
                }
            }
            .filter { it.valid }
            .flatMap { event ->
                mono {
                    groupService.insertGroupUserFromEvent(
                        event.metadata.additionalInfo[AdditionalInfoKey.GROUP_ID]!!,
                        event.user!!
                    )
                }
            }
    }
}