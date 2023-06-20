package edu.messaging.consumers

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun startConsumers() {

    // start an event loop on another thread, to not block the "main" thread (server wouldn't start otherwise)
    Thread({
        runBlocking {
            launch {
                GroupEventConsumer.receive()
            }
            launch {
                GroupUserValidationResultConsumer.receive()
            }
        }
    }, "consumer-starter").start()
}