package edu.connections

import java.util.*

class ConnectionStore {
    companion object {

        private val connections = Collections.synchronizedMap(HashMap<UUID, Connection>())

        fun put(connection: Connection): UUID {
            // generate a new connection id and store the connection with it
            val connectionId = UUID.randomUUID();
            connections[connectionId] = connection
            return connectionId
        }

        fun remove(connectionId: UUID) {
            connections.remove(connectionId)
        }
    }
}