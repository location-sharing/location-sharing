package edu.api.consumers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun startConsumers() {

    // start an event loop on another thread, to not block the "main" thread (server wouldn't start otherwise)
    Thread({
        runBlocking(Dispatchers.IO) {
            ClientMessageConsumer.receive()
        }
    }, "consumer-starter").start()
}