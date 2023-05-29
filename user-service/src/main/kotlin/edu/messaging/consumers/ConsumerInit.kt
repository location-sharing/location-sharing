package edu.messaging.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    userValidationRequestConsumer: UserValidationRequestConsumer
){
    init {
        userValidationRequestConsumer.createFlux().subscribe()
    }
}