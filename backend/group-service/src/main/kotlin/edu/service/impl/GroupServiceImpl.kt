package edu.service.impl

import edu.dto.GroupCreateDto
import edu.dto.GroupPatchDto
import edu.location.sharing.events.notifications.SystemNotification
import edu.location.sharing.events.notifications.UserNotification
import edu.location.sharing.events.validation.user.*
import edu.mapper.GroupMapper
import edu.mapper.UserMapper
import edu.messaging.producers.SystemNotificationProducer
import edu.messaging.producers.UserNotificationProducer
import edu.messaging.producers.UserValidationRequestProducer
import edu.model.Group
import edu.repository.GroupRepository
import edu.repository.UserRepository
import edu.security.jwt.AuthenticatedUser
import edu.service.GroupService
import edu.service.UserGroupService
import edu.service.exception.GroupOperationForbiddenException
import edu.service.exception.ResourceNotFoundException
import edu.service.exception.ServiceException
import edu.service.exception.ValidationException
import edu.util.logger
import edu.util.parseUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class GroupServiceImpl(
    val groupRepository: GroupRepository,
    val userRepository: UserRepository,
    val userValidationRequestProducer: UserValidationRequestProducer,
    val userGroupService: UserGroupService,
    val systemNotificationProducer: SystemNotificationProducer,
    val userNotificationProducer: UserNotificationProducer,

    @Value("\${max_groups_per_user}")
    val maxGroupsPerUser: Int,

    @Value("\${max_users_per_group}")
    val maxUsersPerGroup: Int,
): GroupService {

    val log = logger()

    override suspend fun create(dto: GroupCreateDto, userId: String): Group {

        verifyMaxUserGroupsForUserId(userId)

        val entity = GroupMapper.toEntity(dto, userId)
        val group = withContext(Dispatchers.IO) {
            try {
                groupRepository.save(entity)
            } catch (e: Exception) {
                log.error("Failed to create group", e)
                throw ServiceException("Failed to create group", e)
            }
        }
        log.info("Created group with id ${group.id}")
        // add the owner of the group as a member (with the same flow as adding normal users)
        addGroupUserById(group.id.toString(), loggedInUserId = userId, userToAddId = userId)
        return group
    }

    override suspend fun patch(id: String, dto: GroupPatchDto, userId: String): Group {
        val group = findById(id)
        verifyGroupOwner(group, userId)
        val groupToPatch = GroupMapper.patchEntity(group, dto)
        return withContext(Dispatchers.IO) {
            try {
                val patchedGroup = groupRepository.save(groupToPatch)
                log.info("Patched group with id $id")

                // send to every group user
                group.users.forEach { user ->
                    userNotificationProducer.sendWithResultLogging(
                        UserNotification(
                            UserNotification.Type.SUCCESS,
                            "Info",
                            "Group ${group.name} changed to ${patchedGroup.name}",
                            userId = user.id.toString(),
                            groupId = id
                        )
                    )
                }
                patchedGroup
            } catch (e: Exception) {
                log.error("Failed to update group with id $id", e)
                throw ServiceException("Failed to update group", e)
            }
        }
    }

    /**
     * Embrace blocking calls here because of the transaction manager not being able to handle async calls
     */
    @Transactional
    override fun delete(id: String, userId: String) {
        val group = runBlocking { findById(id) }
        verifyGroupOwner(group, userId)
        try {
            groupRepository.deleteById(group.id!!)
            log.info("Deleted group with id $id")

            runBlocking {
                // system notification so group-dependent services are also consistent
                val notification = SystemNotification(
                    SystemNotification.Type.GROUP_DELETE,
                    userId = userId,
                    groupId = id
                )
                systemNotificationProducer.sendWithResultLogging(notification)
                log.info("sent system notification $notification")

                // send to every group user
                group.users.forEach {user ->
                    userNotificationProducer.sendWithResultLogging(
                        UserNotification(
                            UserNotification.Type.SUCCESS,
                            "Success",
                            "Group ${group.name} removed",
                            userId = user.id.toString(),
                            groupId = id
                        )
                    )
                }
            }
        } catch (e: Exception) {
            log.error("Failed to delete group with id $id", e)
            throw ServiceException("Failed to delete group", e)
        }
    }

    /**
     * Sends out a validation event to fetch data about the user we're trying to add
     */
    override suspend fun addGroupUserById(groupId: String, loggedInUserId: String, userToAddId: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, loggedInUserId)

        val uuid = parseUuid(userToAddId)

        // user is already in the group
        if (group.users.find { it.id == uuid } != null) {
            log.debug("User with id $userToAddId is already in group with id $groupId")
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
        log.info("Requested user with id $userToAddId to be added to group with id $groupId")
    }

    /**
     * Sends out a validation event to fetch data about the user we're trying to add
     */
    override suspend fun addGroupUserByUsername(groupId: String, loggedInUserId: String, username: String) {
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
        log.info("Requested user with username $username to be added to group with id $groupId")
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    override suspend fun addGroupUserFromEvent(groupId: String, ownerId: String, userEvent: UserEvent) {
        val group = findById(groupId)

        val userToAdd = UserMapper.from(userEvent)

        // only add to the set, let cascading do the rest
        group.users.add(userToAdd)

        withContext(Dispatchers.IO) {
            try {
                val updatedGroup = groupRepository.save(group)
                // send to everyone in the group
                updatedGroup.users.forEach { user ->
                    userNotificationProducer.sendWithResultLogging(
                        UserNotification(
                            UserNotification.Type.SUCCESS,
                            "Info",
                            "User ${userToAdd.username} added to group ${group.name}",
                            userId = user.id.toString(),
                            groupId = groupId,
                            groupName = group.name
                        )
                    )
                }
                updatedGroup
            } catch (e: Exception) {
                log.error("Failed to save group $groupId with added user ${userEvent.userId}", e)
                userNotificationProducer.sendWithResultLogging(
                    UserNotification(
                        UserNotification.Type.ERROR,
                        "Error",
                        "Failed to add ${userToAdd.username} to group ${group.name}",
                        userId = ownerId,
                        groupId = groupId,
                        groupName = group.name
                    )
                )
            }
        }
    }

    override suspend fun removeGroupUserById(groupId: String, loggedInUserId: String, removeUserId: String) {
        val group = userGroupService.findUserGroup(loggedInUserId, groupId)

        verifyGroupUserRemoval(
            userIsGroupOwner = loggedInUserId == group.ownerId,
            userTryingToRemoveItself = loggedInUserId == removeUserId
        )

        // remaining scenarios are ok
        // owner of the group can remove any other group member, or any group member can remove themselves
        val removeUserUUID = parseUuid(removeUserId)
        group.users.removeIf { it.id == removeUserUUID }

        withContext(Dispatchers.IO) {
            try {
                groupRepository.save(group)
                // system notification so other services update their data
                val notification = SystemNotification(
                    SystemNotification.Type.GROUP_USER_DELETE,
                    userId = removeUserId,
                    groupId = groupId
                )
                systemNotificationProducer.sendWithResultLogging(notification)
                log.info("sent system notification $notification")
                userNotificationProducer.sendWithResultLogging(
                    UserNotification(
                        UserNotification.Type.SUCCESS,
                        "Info",
                        "You have been removed from group ${group.name}",
                        userId = removeUserId,
                        groupId = groupId,
                    )
                )

            } catch (e: Exception) {
                log.error("Removing user $removeUserId from group $groupId failed", e)
                throw ServiceException("Removing user $removeUserId from group failed")
            }
        }
    }

    override suspend fun removeGroupUserByUsername(groupId: String, user: AuthenticatedUser, removeUsername: String) {
        val group = userGroupService.findUserGroup(user.id, groupId)

        verifyGroupUserRemoval(
            userIsGroupOwner = user.id == group.ownerId,
            userTryingToRemoveItself = user.username == removeUsername
        )

        // remaining scenarios are ok
        // owner of the group can remove any other group member, or any group member can remove themselves
        group.users.removeIf { it.username == removeUsername }

        withContext(Dispatchers.IO) {
            try {
                groupRepository.save(group)
                // system notification so other services update their data
                val notification = SystemNotification(
                    SystemNotification.Type.GROUP_USER_DELETE,
                    username = removeUsername,
                    groupId = groupId,
                    groupName = group.name
                )
                systemNotificationProducer.sendWithResultLogging(notification)
                log.info("sent system notification $notification")
                userNotificationProducer.sendWithResultLogging(
                    UserNotification(
                        UserNotification.Type.SUCCESS,
                        "Info",
                        "You have been removed from group ${group.name}",
                        username = removeUsername,
                        groupId = groupId,
                        groupName = group.name
                    )
                )

            } catch (e: Exception) {
                log.error("Removing user $removeUsername from group $groupId failed", e)
                throw ServiceException("Removing user $removeUsername from group failed")
            }
        }
    }

    private fun verifyGroupUserRemoval(userIsGroupOwner: Boolean, userTryingToRemoveItself: Boolean) {
        if (userIsGroupOwner && userTryingToRemoveItself) {
            val e = ValidationException("You can't remove yourself because you are the owner of the group")
            log.debug("Group owner can't remove itself", e)
            throw e
        }

        if (!userIsGroupOwner && !userTryingToRemoveItself) {
            val e = GroupOperationForbiddenException("Only the group owner can remove other members")
            log.debug("Only the group owner can remove other members", e)
            throw e
        }
    }

    override suspend fun changeOwnerById(groupId: String, loggedInUserId: String, newOwnerId: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, loggedInUserId)

        if (loggedInUserId == newOwnerId) {
            log.debug("User $loggedInUserId is already the owner of group $groupId")
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
        log.info("Requested user $newOwnerId to be the owner of group with id $groupId")
    }

    override suspend fun changeOwnerByUsername(groupId: String, user: AuthenticatedUser, newOwnerUsername: String) {
        val group = findById(groupId)
        verifyGroupOwner(group, user.id)

        if (user.username == newOwnerUsername) {
            log.debug("User $newOwnerUsername is already the owner of group $groupId")
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
        log.info("Requested user $newOwnerUsername to be the owner of group with id $groupId")
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    override suspend fun changeOwnerFromEvent(groupId: String, currentOwnerId: String, userEvent: UserEvent) {
        val group = findById(groupId)

        group.ownerId = userEvent.userId

        // if the new owner is not in the group, add it
        val newOwnerUUID = parseUuid(userEvent.userId)
        if (group.users.find { it.id == newOwnerUUID } == null) {
            group.users.add(UserMapper.from(userEvent))
        }

        withContext(Dispatchers.IO) {
            try {
                groupRepository.save(group)
                userNotificationProducer.sendWithResultLogging(
                    UserNotification(
                        UserNotification.Type.SUCCESS,
                        "Info",
                        "You have been appointed to the owner of group ${group.name}",
                        userId = userEvent.userId,
                        username = userEvent.username,
                        groupId = groupId,
                        groupName = group.name
                    )
                )
            } catch (e: Exception) {
                log.error("Failed to save group $groupId when changing owner to ${userEvent.userId}", e)
            }
        }
    }

    private suspend fun findById(id: String): Group {
        val groupUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw ValidationException("User id is not a valid UUID", e)
        }

        return withContext(Dispatchers.IO) {
            groupRepository
                .findById(groupUUID)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found") }
        }
    }

    private fun verifyGroupOwner(group: Group, ownerId: String) {
        if (group.ownerId != ownerId) {
            val e = GroupOperationForbiddenException("Only the group owner can perform this operation")
            log.debug("User is not the group owner for this operation", e)
            throw e
        }
    }

    private fun verifyMaxGroupUsers(group: Group) {
        if (group.users.size >= maxUsersPerGroup) {
            val e = ValidationException("Groups can't have more than $maxUsersPerGroup users")
            log.debug("Group ${group.id} has more than $maxUsersPerGroup users", e)
            throw e
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
            val e = ValidationException("You can't have more than $maxGroupsPerUser groups as an owner")
            log.debug("User has more than $maxGroupsPerUser groups as an owner", e)
            throw e
        }
    }
}