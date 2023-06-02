package edu.repository

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ConnectionStore {


    private val connections = ConcurrentHashMap<String, GroupConnections>()

    fun storeConnection(groupId: String, userId: String, connection: WebSocketSession) {
        if (connections.containsKey(groupId)) {
            connections[groupId]!!.addUserConnection(userId, connection)
        } else {
            connections[groupId] = GroupConnections().apply { addUserConnection(userId, connection) }
        }
    }

    fun removeConnection(groupId: String, userId: String, connection: WebSocketSession) {
        connections[groupId]?.removeUserConnection(userId, connection)
    }

    fun getGroupConnections(groupId: String): List<WebSocketSession> {
        return if (connections.containsKey(groupId)) {
            connections[groupId]!!.getConnections()
        } else {
            listOf()
        }
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
            groupConnections[userId]?.removeConnection(connection)
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