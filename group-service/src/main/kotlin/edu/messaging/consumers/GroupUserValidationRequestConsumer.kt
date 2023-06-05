package edu.messaging.consumers

import edu.location.sharing.events.validation.group.user.GroupUserValidationRequestEvent
import edu.location.sharing.events.validation.group.user.GroupUserValidationResultEvent
import edu.messaging.config.KafkaConfig
import edu.messaging.producers.GroupUserValidationResultProducer
import edu.service.ResourceNotFoundException
import edu.service.UserGroupService
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class GroupUserValidationRequestConsumer(
    kafkaConfig: KafkaConfig,
    private val userGroupService: UserGroupService,
    private val groupUserValidationResultProducer: GroupUserValidationResultProducer,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.groupUserValidationRequestTopic
) {

    override val log = logger()

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), GroupUserValidationRequestEvent::class.java) }
            .doOnError { log.warn("error while parsing group user validation request: $it") }
            .flatMap {
                mono {
                    try {
                        userGroupService.findUserGroup(it.userId, it.groupId)

                        // send a positive validation response
                        val response = GroupUserValidationResultEvent(
                            groupId = it.groupId,
                            userId = it.userId,
                            metadata = it.metadata,
                            valid = true,
                            message = null,
                        )
                        groupUserValidationResultProducer.sendWithResultLogging(response)
                    } catch (e: ResourceNotFoundException) {
                        // send a negative validation response
                        val response = GroupUserValidationResultEvent(
                            groupId = it.groupId,
                            userId = it.userId,
                            metadata = it.metadata,
                            valid = false,
                            message = "Group with id ${it.groupId} does not exist for user with id ${it.userId}",
                        )
                        groupUserValidationResultProducer.sendWithResultLogging(response)
                    } catch (e: Exception) {
                        // send a negative validation response
                        val response = GroupUserValidationResultEvent(
                            groupId = it.groupId,
                            userId = it.userId,
                            metadata = it.metadata,
                            valid = false,
                            message = "Unknown error occurred",
                        )
                        groupUserValidationResultProducer.sendWithResultLogging(response)
                    }
                }
            }
    }
}