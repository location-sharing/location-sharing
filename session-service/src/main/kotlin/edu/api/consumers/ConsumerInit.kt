package edu.api.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    connectionEventConsumer: ConnectionEventConsumer,
    clientMessageConsumer: ClientMessageConsumer,
){
    init {
        connectionEventConsumer.createFlux().subscribe()
        clientMessageConsumer.createFlux().subscribe()
    }
}