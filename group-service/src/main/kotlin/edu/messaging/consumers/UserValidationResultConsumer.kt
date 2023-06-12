package edu.messaging.consumers

import edu.location.sharing.events.validation.user.AdditionalInfoKey
import edu.location.sharing.events.validation.user.UserValidationPurpose
import edu.location.sharing.events.validation.user.UserValidationResultEvent
import edu.messaging.config.KafkaConfig
import edu.service.GroupService
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class UserValidationResultConsumer(
    kafkaConfig: KafkaConfig,
    private val groupService: GroupService,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.userValidationResponseTopic
) {

    override val log = logger()

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), UserValidationResultEvent::class.java) }
            .doOnError { log.warn("error while parsing user validation result: $it") }
            .filter {
                it.metadata.purpose == UserValidationPurpose.GROUP_ADD_USER ||
                it.metadata.purpose == UserValidationPurpose.GROUP_CHANGE_OWNER
            }
            .doOnNext {
                if (!it.valid) {
                    // TODO: send notification about the validation status
                    log.warn("pending request for user invalid: ${it.message}")
                }
            }
            .filter { it.valid }
            .flatMap { event ->
                val groupId = event.metadata.additionalInfo[AdditionalInfoKey.GROUP_ID]!!
                val ownerId = event.metadata.initiatorUserId
                val validatedUser = event.user!!
                when(event.metadata.purpose) {
                    UserValidationPurpose.GROUP_ADD_USER -> mono {
                        groupService.addGroupUserFromEvent(groupId, ownerId, validatedUser)
                    }
                    UserValidationPurpose.GROUP_CHANGE_OWNER -> mono {
                        groupService.changeOwnerFromEvent(groupId, ownerId, validatedUser)
                    }
                }
            }
    }
}