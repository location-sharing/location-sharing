package edu.messaging.consumers

import kotlinx.coroutines.runBlocking

fun startConsumers() {

    // start an event loop on another thread, to not block the "main" thread (server wouldn't start otherwise)
    Thread({
        runBlocking {
            GroupEventConsumer.receive()
            GroupUserValidationResultConsumer.receive()
        }
    }, "consumer-starter").start()
}