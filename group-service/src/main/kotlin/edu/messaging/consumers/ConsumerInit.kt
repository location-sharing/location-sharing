package edu.messaging.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    userValidationResultConsumer: UserValidationResultConsumer,
    groupUserValidationRequestConsumer: GroupUserValidationRequestConsumer,
){
    init {
        userValidationResultConsumer.createFlux().subscribe()
        groupUserValidationRequestConsumer.createFlux().subscribe()

    }
}