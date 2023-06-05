package edu.repository

import edu.location.sharing.util.logger
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ConnectionStore {

    private val log = logger()
    private val connections = ConcurrentHashMap<String, GroupConnections>()

    fun storeConnection(groupId: String, userId: String, connection: WebSocketSession) {
        if (connections.containsKey(groupId)) {
            connections[groupId]!!.addUserConnection(userId, connection)
        } else {
            connections[groupId] = GroupConnections().apply { addUserConnection(userId, connection) }
        }

        log.info("after store connection: ")
        connections.forEach { (k, v) ->
            log.info("$k group connections:")
            v.getUserConnections(userId).forEach {
                log.info("$k  -  $userId  -  $it")
            }
        }
    }

    fun removeConnection(groupId: String, userId: String, connection: WebSocketSession) {
        if (connections.containsKey(groupId)) {
            connections[groupId]!!.removeUserConnection(userId, connection)

            if (connections[groupId]!!.getConnections().isEmpty()) {
                connections.remove(groupId)
            }
        }

        log.info("after remove connection: ")
        connections.forEach { (k, v) ->
            log.info("$k group connections:")
            v.getUserConnections(userId).forEach {
                log.info("$k  -  $userId  -  $it")
            }
        }
    }

    fun getGroupConnections(groupId: String): List<WebSocketSession> {
        return if (connections.containsKey(groupId)) {
            connections[groupId]!!.getConnections()
        } else {
            listOf()
        }
    }

    fun userGroupConnectionsEmpty(groupId: String, userId: String): Boolean {
        if (connections.containsKey(groupId)) {
            val userGroupConnections = connections[groupId]!!.getUserConnections(userId)
            return userGroupConnections.isEmpty()
        }
        return true
    }

    fun connectionExists(groupId: String, userId: String, connection: WebSocketSession): Boolean {
        return connections.containsKey(groupId) && connections[groupId]!!.getUserConnections(userId).contains(connection)
    }

    fun isTheOnlyConnection(groupId: String, userId: String, connection: WebSocketSession): Boolean {
        if (connections.containsKey(groupId)) {
            val userConnections = connections[groupId]!!.getUserConnections(userId)
            return userConnections.size == 1 && userConnections.contains(connection)
        }
        return false
    }

    private class GroupConnections {
        private val groupConnections = ConcurrentHashMap<String, UserConnections>()

        fun addUserConnection(userId: String, connection: WebSocketSession) {
            if (groupConnections.containsKey(userId)) {
                groupConnections[userId]!!.addConnection(connection)
            } else {
                groupConnections[userId] = UserConnections().apply { addConnection(connection) }
            }
        }

        fun removeUserConnection(userId: String, connection: WebSocketSession) {
            if (groupConnections.containsKey(userId)) {
                groupConnections[userId]!!.removeConnection(connection)

                if (groupConnections[userId]!!.getConnections().isEmpty()) {
                    groupConnections.remove(userId)
                }
            }
        }

        fun getUserConnections(userId: String): List<WebSocketSession> {
            return if (groupConnections.containsKey(userId)) {
                groupConnections[userId]!!.getConnections()
            } else {
                listOf()
            }
        }

        fun getConnections(): List<WebSocketSession> {
            return groupConnections.values.flatMap { it.getConnections() }
        }
    }

    private class UserConnections {
        private val userConnections = CopyOnWriteArrayList<WebSocketSession>()
        fun addConnection(connection: WebSocketSession) {
            userConnections.add(connection)
        }

        fun removeConnection(connection: WebSocketSession) {
            userConnections.remove(connection)
        }

        fun getConnections(): List<WebSocketSession> {
            return userConnections
        }
    }
}