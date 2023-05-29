package edu.location.sharing.models.events.connections.headers

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader

class EventTypeKafkaHeader(
    type: EventType
): RecordHeader(
    key,
    ByteArray(1) { type.id }
) {
    companion object {
        const val key = "eventType"

        fun getEventType(headers: Headers): EventType? {
            if (headers.headers(key).any()) {
                val header = headers.lastHeader(key)
                val typeId = header.value()[0]
                return EventType.idToEventMap[typeId]
            }
            return null
        }

        fun getEventType(record: ConsumerRecord<*, *>): EventType? {
          return getEventType(record.headers())
        }
    }
}