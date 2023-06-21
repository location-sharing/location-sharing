package edu.service

import edu.model.Group

interface UserGroupService {
    suspend fun findUserGroupsById(userId: String): Set<Group>
    suspend fun findUserGroupsByUsername(username: String): Set<Group>
    suspend fun findUserGroup(userId: String, groupId: String): Group
    suspend fun updateUser(userId: String, username: String)
    suspend fun deleteUser(userId: String)
}