package edu.messaging.consumers

import edu.location.sharing.events.notifications.SystemNotification
import edu.messaging.config.KafkaConfig
import edu.service.UserGroupService
import edu.service.exception.ServiceException
import edu.util.logger
import edu.util.objectMapper
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class SystemNotificationConsumer(
    kafkaConfig: KafkaConfig,
    private val userGroupService: UserGroupService,
): GenericConsumer(
    kafkaConfig,
    kafkaConfig.systemNotificationsTopic
) {
    override val log = logger()

    override fun processFlux(flux: Flux<ConsumerRecord<String, ByteArray>>): Flux<*> {
        return flux
            .map { objectMapper.readValue(it.value(), SystemNotification::class.java) }
            .doOnError { log.error("error while parsing system notification", it) }
            .flatMap { notification ->
                when(notification.type) {
                    SystemNotification.Type.USER_DELETE -> {
                        mono {
                            if (notification.userId == null) {
                                return@mono
                            }
                            log.info("received user delete system notification")
                            userGroupService.deleteUser(notification.userId!!)
                        }
                    }
                    SystemNotification.Type.USER_UPDATE -> {
                        mono {
                            if (notification.userId == null || notification.username == null) {
                                return@mono
                            }
                            log.info("received user update system notification")
                            try {
                                userGroupService.updateUser(
                                    notification.userId!!,
                                    notification.username!!
                                )
                            } catch (e: ServiceException) {
                                log.error("Error while consuming user update system notification", e)
                            }
                        }
                    }
                    else -> Mono.empty()
                }
            }

    }
}