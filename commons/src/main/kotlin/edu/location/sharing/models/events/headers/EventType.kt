package edu.location.sharing.models.events.headers

import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent

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