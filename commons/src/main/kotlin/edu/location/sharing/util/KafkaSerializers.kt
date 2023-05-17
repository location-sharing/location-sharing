package edu.location.sharing.util

import edu.location.sharing.models.events.ClientMessageEvent
import edu.location.sharing.models.events.ConnectionEvent
import edu.location.sharing.models.events.RemoveConnectionEvent
import edu.location.sharing.models.events.StoreConnectionEvent
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

interface ConnectionEventDeserializer<T: ConnectionEvent>: Deserializer<T>

object StoreConnectionEventDeserializer: ConnectionEventDeserializer<StoreConnectionEvent> {
    override fun deserialize(topic: String?, data: ByteArray?): StoreConnectionEvent {
        return objectMapper.readValue(data, StoreConnectionEvent::class.java)
    }
}

object RemoveConnectionEventDeserializer: ConnectionEventDeserializer<RemoveConnectionEvent> {
    override fun deserialize(topic: String?, data: ByteArray?): RemoveConnectionEvent {
        return objectMapper.readValue(data, RemoveConnectionEvent::class.java)
    }
}

object ClientMessageEventDeserializer: Deserializer<ClientMessageEvent> {
    override fun deserialize(topic: String?, data: ByteArray?): ClientMessageEvent {
        return objectMapper.readValue(data, ClientMessageEvent::class.java)
    }
}

class JsonSerializer<T>: Serializer<T> {
    override fun serialize(topic: String?, data: T): ByteArray {
        return objectMapper.writeValueAsBytes(data)
    }
}
