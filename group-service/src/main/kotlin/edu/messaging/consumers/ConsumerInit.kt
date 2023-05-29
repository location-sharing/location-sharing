package edu.messaging.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    userValidationResponseConsumer: UserValidationResponseConsumer
){
    init {
        userValidationResponseConsumer.createFlux().subscribe()
    }
}