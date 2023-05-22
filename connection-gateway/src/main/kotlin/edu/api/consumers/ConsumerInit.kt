package edu.api.consumers

import kotlinx.coroutines.runBlocking

fun startConsumers() {

    // start an event loop on another thread, to not block the "main" thread (server wouldn't start otherwise)
    Thread({
        runBlocking {
            ClientMessageConsumer.receive()
        }
    }, "consumer-starter").start()
}