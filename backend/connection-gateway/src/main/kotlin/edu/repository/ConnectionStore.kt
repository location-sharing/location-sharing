package edu.repository

import edu.location.sharing.util.logger
import edu.security.AuthenticatedUser
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ConnectionStore {

    private val log = logger()
    private val connections = ConcurrentHashMap<String, GroupConnections>()

    fun storeConnection(groupId: String, user: AuthenticatedUser, connection: WebSocketSession) {
        if (connections.containsKey(groupId)) {
            connections[groupId]!!.addUserConnection(user, connection)
        } else {
            connections[groupId] = GroupConnections().apply { addUserConnection(user, connection) }
        }

        log.info("after store connection: ")
        connections.forEach { (k, v) ->
            log.info("$k group connections:")
            v.getUserConnections(user).forEach {
                log.info("$k  -  $user  -  $it")
            }
        }
    }

    fun removeConnection(groupId: String, user: AuthenticatedUser, connection: WebSocketSession) {
        if (connections.containsKey(groupId)) {
            connections[groupId]!!.removeUserConnection(user, connection)

            if (connections[groupId]!!.getConnections().isEmpty()) {
                connections.remove(groupId)
            }
        }

        log.info("after remove connection: ")
        connections.forEach { (k, v) ->
            log.info("$k group connections:")
            v.getUserConnections(user).forEach {
                log.info("$k  -  $user  -  $it")
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

    fun getGroupUsers(groupId: String): List<AuthenticatedUser> {
        return if (connections.containsKey(groupId)) {
            connections[groupId]!!.getUsers()
        } else {
            listOf()
        }
    }

    fun userGroupConnectionsEmpty(groupId: String, user: AuthenticatedUser): Boolean {
        if (connections.containsKey(groupId)) {
            val userGroupConnections = connections[groupId]!!.getUserConnections(user)
            return userGroupConnections.isEmpty()
        }
        return true
    }

    fun connectionExists(groupId: String, user: AuthenticatedUser, connection: WebSocketSession): Boolean {
        return connections.containsKey(groupId) && connections[groupId]!!.getUserConnections(user).contains(connection)
    }

    fun isTheOnlyConnection(groupId: String, user: AuthenticatedUser, connection: WebSocketSession): Boolean {
        if (connections.containsKey(groupId)) {
            val userConnections = connections[groupId]!!.getUserConnections(user)
            return userConnections.size == 1 && userConnections.contains(connection)
        }
        return false
    }

    private class GroupConnections {
        private val groupConnections = ConcurrentHashMap<AuthenticatedUser, UserConnections>()

        fun addUserConnection(user: AuthenticatedUser, connection: WebSocketSession) {
            if (groupConnections.containsKey(user)) {
                groupConnections[user]!!.addConnection(connection)
            } else {
                groupConnections[user] = UserConnections().apply { addConnection(connection) }
            }
        }

        fun removeUserConnection(user: AuthenticatedUser, connection: WebSocketSession) {
            if (groupConnections.containsKey(user)) {
                groupConnections[user]!!.removeConnection(connection)

                if (groupConnections[user]!!.getConnections().isEmpty()) {
                    groupConnections.remove(user)
                }
            }
        }

        fun getUserConnections(user: AuthenticatedUser): List<WebSocketSession> {
            return if (groupConnections.containsKey(user)) {
                groupConnections[user]!!.getConnections()
            } else {
                listOf()
            }
        }

        fun getConnections(): List<WebSocketSession> {
            return groupConnections.values.flatMap { it.getConnections() }
        }

        fun getUsers(): List<AuthenticatedUser> {
            val list = ArrayList<AuthenticatedUser>(groupConnections.size)
            list.addAll(groupConnections.keys().toList())
            return list
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