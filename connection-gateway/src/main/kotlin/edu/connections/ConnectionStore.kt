package edu.connections

import java.util.*

class ConnectionStore {
    companion object {

        private val userConnections = Collections.synchronizedMap(HashMap<String, MutableSet<UUID>>())
        private val connectionUsers = Collections.synchronizedMap(HashMap<UUID, String>())
        private val connections = Collections.synchronizedMap(HashMap<UUID, Connection>())

        fun put(userId: String, connection: Connection): UUID {

            // generate a new connection id and store the connection with it
            val connectionId = UUID.randomUUID();
            connections[connectionId] = connection

            // store the connection under the user
            if (userId !in userConnections) {
                val userConnectionIds = HashSet<UUID>()
                userConnectionIds.add(connectionId)
                userConnections[userId] = userConnectionIds
            } else {
                userConnections[userId]!!.add(connectionId)
            }

            // save the inverse mapping
            connectionUsers[connectionId] = userId

            return connectionId
        }

        fun remove(userId: String) {
            if (userId !in userConnections) {
                return
            }
            for (connectionId in userConnections[userId]!!) {
                connectionUsers.remove(connectionId)
                connections.remove(connectionId)
            }
            userConnections.remove(userId)
        }

        fun removeConnection(connectionId: UUID) {

            if (connectionId !in connections) {
                // connection doesn't exist
                return
            }

            // remove the connection from the user's list
            val userId = connectionUsers[connectionId]!!
            userConnections[userId]?.remove(connectionId)

            connections.remove(connectionId)
        }
    }
}