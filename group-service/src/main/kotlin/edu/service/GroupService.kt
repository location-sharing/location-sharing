package edu.service

import edu.dto.GroupCreateDto
import edu.dto.GroupPatchDto
import edu.location.sharing.events.validation.user.UserEvent
import edu.model.Group
import edu.security.jwt.AuthenticatedUser

interface GroupService {

    suspend fun create(dto: GroupCreateDto, userId: String): Group
    suspend fun patch(id: String, dto: GroupPatchDto, userId: String): Group
    fun delete(id: String, userId: String)

    suspend fun addGroupUserById(groupId: String, loggedInUserId: String, userToAddId: String)
    suspend fun addGroupUserByUsername(groupId: String, loggedInUserId: String, username: String)
    suspend fun addGroupUserFromEvent(groupId: String, ownerId: String, userEvent: UserEvent)

    suspend fun removeGroupUserById(groupId: String, loggedInUserId: String, removeUserId: String)
    suspend fun removeGroupUserByUsername(groupId: String, user: AuthenticatedUser, removeUsername: String)


    suspend fun changeOwnerById(groupId: String, loggedInUserId: String, newOwnerId: String)
    suspend fun changeOwnerByUsername(groupId: String, user: AuthenticatedUser, newOwnerUsername: String)
    suspend fun changeOwnerFromEvent(groupId: String, currentOwnerId: String, userEvent: UserEvent)
}

