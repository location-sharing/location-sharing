package edu.location.sharing.models.events.connections.headers

import edu.location.sharing.models.events.connections.ClientMessageEvent
import edu.location.sharing.models.events.connections.RemoveConnectionEvent
import edu.location.sharing.models.events.connections.StoreConnectionEvent

enum class EventType(
    val id: Byte,
    val clazz: Class<*>
) {

    STORE_CONNECTION(0x00, StoreConnectionEvent::class.java),
    REMOVE_CONNECTION(0x01, RemoveConnectionEvent::class.java),
    CLIENT_MESSAGE(0x02, ClientMessageEvent::class.java);

    companion object TypeMap {
        val idToEventMap = mapOf(
            0x00.toByte() to STORE_CONNECTION,
            0x01.toByte() to REMOVE_CONNECTION,
            0x02.toByte() to CLIENT_MESSAGE,
        )
    }
}