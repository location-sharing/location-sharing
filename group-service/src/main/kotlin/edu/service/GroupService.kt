package edu.service

import edu.controller.exception.ForbiddenException
import edu.dto.GroupCreateDto
import edu.dto.GroupUpdateDto
import edu.location.sharing.events.validation.user.*
import edu.mapper.GroupMapper
import edu.mapper.UserMapper
import edu.messaging.producers.UserValidationRequestProducer
import edu.repository.GroupRepository
import edu.repository.model.Group
import edu.security.jwt.AuthenticatedUser
import edu.service.exception.ValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class GroupService(
    val groupRepository: GroupRepository,
    val userValidationRequestProducer: UserValidationRequestProducer,
    val userGroupService: UserGroupService,

    @Value("\${max_groups_per_user}")
    val maxGroupsPerUser: Int,

    @Value("\${max_users_per_group}")
    val maxUsersPerGroup: Int,
) {

    suspend fun create(dto: GroupCreateDto, ownerId: String): Group {

        verifyMaxUserGroupsForUserId(ownerId)

        val entity = GroupMapper.toEntity(dto, ownerId)
        val group = withContext(Dispatchers.IO) {
            groupRepository.save(entity)
        }

        // add the owner of the group as a member (with the same flow as adding normal users)
        addGroupUserById(group.id.toString(), loggedInUserId = ownerId, userToAddId = ownerId)
        return group
    }

    suspend fun patch(id: String, dto: GroupUpdateDto, userId: String): Group {
        val group = findById(id)
        verifyGroupOwner(group, userId)
        val patchedGroup = GroupMapper.patchEntity(group, dto)
        return withContext(Dispatchers.IO) {
            groupRepository.save(patchedGroup)
        }
    }

    /**
     * Embrace blocking calls here because of the transaction manager not being able to handle async calls
     */
    @Transactional
     fun delete(id: String, userId: String) {
        val group = runBlocking { findById(id) }
        verifyGroupOwner(group, userId)
        groupRepository.deleteById(UUID.fromString(id))
    }

    /**
     * Sends out a validation event to fetch data about the user we're trying to add
     */
    suspend fun addGroupUserById(groupId: String, loggedInUserId: String, userToAddId: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, loggedInUserId)

        // user is already in the group
        if (group.users.find { it.id == UUID.fromString(userToAddId) } != null) {
            return
        }

        verifyMaxGroupUsers(group)
        verifyMaxUserGroupsForUserId(userToAddId)

        val metadata = UserValidationMetadata(
            initiatorUserId = loggedInUserId,
            purpose = UserValidationPurpose.GROUP_ADD_USER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(
                userId = userToAddId,
                metadata = metadata)
        )
    }

    /**
     * Sends out a validation event to fetch data about the user we're trying to add
     */
    suspend fun addGroupUserByUsername(groupId: String, loggedInUserId: String, username: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, loggedInUserId)

        // user is already in the group
        if (group.users.find { it.username == username } != null) {
            return
        }

        verifyMaxGroupUsers(group)
        verifyMaxUserGroupsForUsername(username)

        val metadata = UserValidationMetadata(
            initiatorUserId = loggedInUserId,
            purpose = UserValidationPurpose.GROUP_ADD_USER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(
                username = username,
                metadata = metadata)
        )
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    suspend fun addGroupUserFromEvent(groupId: String, ownerId: String, userEvent: UserEvent): Group {
        val group = findById(groupId)

        val userToAdd = UserMapper.from(userEvent)

        // only add to the set, let cascading do the rest
        group.users.add(userToAdd)

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun removeGroupUserById(groupId: String, loggedInUserId: String, removeUserId: String) {
        val group = userGroupService.findUserGroup(loggedInUserId, groupId)

        val userIsGroupOwner = loggedInUserId == group.ownerId
        val userTryingToRemoveItself = loggedInUserId == removeUserId

        if (userIsGroupOwner && userTryingToRemoveItself) {
            throw ValidationException("You can't remove yourself because you are the owner of the group")
        }

        if (!userIsGroupOwner && !userTryingToRemoveItself) {
            throw ForbiddenException("Only the group owner can remove other members")
        }

        // remaining scenarios are ok
        // owner of the group can remove any other group member, or any group member can remove themselves
        val removeUserUUID = UUID.fromString(removeUserId)
        group.users.removeIf { it.id == removeUserUUID }

        withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun removeGroupUserByUsername(groupId: String, user: AuthenticatedUser, removeUsername: String) {
        val group = userGroupService.findUserGroup(user.id, groupId)

        val userIsGroupOwner = user.id == group.ownerId
        val userTryingToRemoveItself = user.username == removeUsername

        if (userIsGroupOwner && userTryingToRemoveItself) {
            throw ValidationException("You can't remove yourself because you are the owner of the group")
        }

        if (!userIsGroupOwner && !userTryingToRemoveItself) {
            throw ForbiddenException("Only the group owner can remove other members")
        }

        // remaining scenarios are ok
        // owner of the group can remove any other group member, or any group member can remove themselves
        group.users.removeIf { it.username == removeUsername }

        withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun changeOwnerById(groupId: String, loggedInUserId: String, newOwnerId: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, loggedInUserId)

        if (loggedInUserId == newOwnerId) {
            return
        }

        val metadata = UserValidationMetadata(
            initiatorUserId = loggedInUserId,
            purpose = UserValidationPurpose.GROUP_CHANGE_OWNER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(
                userId = newOwnerId,
                metadata = metadata)
        )
    }

    suspend fun changeOwnerByUsername(groupId: String, user: AuthenticatedUser, newOwnerUsername: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, user.id)

        if (user.username == newOwnerUsername) {
            return
        }

        val metadata = UserValidationMetadata(
            initiatorUserId = user.id,
            purpose = UserValidationPurpose.GROUP_CHANGE_OWNER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(
                username = newOwnerUsername,
                metadata = metadata)
        )
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    suspend fun changeOwnerFromEvent(groupId: String, currentOwnerId: String, userEvent: UserEvent) {
        val group = findById(groupId)

        group.ownerId = userEvent.userId

        // if the new owner is not in the group, add it
        val newOwnerUUID = UUID.fromString(userEvent.userId)
        if (group.users.find { it.id == newOwnerUUID } == null) {
            group.users.add(UserMapper.from(userEvent))
        }

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    private suspend fun findById(id: String): Group {
        val groupUUID = UUID.fromString(id)

        return withContext(Dispatchers.IO) {
            groupRepository
                .findById(groupUUID)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found") }
        }
    }

    private fun verifyGroupOwner(group: Group, ownerId: String) {
        if (group.ownerId != ownerId) {
            throw ForbiddenException("Only the group owner can perform this operation")
        }
    }

    private fun verifyMaxGroupUsers(group: Group) {
        if (group.users.size >= maxUsersPerGroup) {
            throw ValidationException("Groups can't have more than $maxUsersPerGroup users")
        }
    }

    private suspend fun verifyMaxUserGroupsForUserId(userId: String) {
        val userGroups = userGroupService.findUserGroupsById(userId)
        verifyMaxUserGroups(userGroups)
    }

    private suspend fun verifyMaxUserGroupsForUsername(username: String) {
        val userGroups = userGroupService.findUserGroupsByUsername(username)
       verifyMaxUserGroups(userGroups)
    }

    private fun verifyMaxUserGroups(userGroups: Set<Group>) {
        if (userGroups.size >= maxGroupsPerUser) {
            throw ValidationException("You can't have more than $maxGroupsPerUser groups as an owner")
        }
    }
}