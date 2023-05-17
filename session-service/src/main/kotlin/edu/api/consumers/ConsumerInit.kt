package edu.api.consumers

import org.springframework.stereotype.Service

@Service
class ConsumerInit(
    storeConnectionConsumer: StoreConnectionConsumer,
    removeConnectionConsumer: RemoveConnectionConsumer,
){
    init {
        storeConnectionConsumer.createFlux().subscribe()
        removeConnectionConsumer.createFlux().subscribe()
    }
}