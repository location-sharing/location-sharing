package edu.messaging.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    userValidationResultConsumer: UserValidationResultConsumer,
    groupUserValidationRequestConsumer: GroupUserValidationRequestConsumer,
    systemNotificationConsumer: SystemNotificationConsumer,
){
    init {
        userValidationResultConsumer.createFlux().retry().subscribe()
        groupUserValidationRequestConsumer.createFlux().retry().subscribe()
        systemNotificationConsumer.createFlux().retry().subscribe()
    }
}