package edu.service.impl

import edu.location.sharing.events.notifications.UserNotification
import edu.messaging.producers.UserNotificationProducer
import edu.model.Group
import edu.repository.GroupRepository
import edu.repository.UserRepository
import edu.service.UserGroupService
import edu.service.exception.ResourceNotFoundException
import edu.service.exception.ServiceException
import edu.util.logger
import edu.util.parseUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class UserGroupServiceImpl(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val userNotificationProducer: UserNotificationProducer,
): UserGroupService {

    val log = logger()

    override suspend fun findUserGroupsById(userId: String): Set<Group> {
        val uuid = parseUuid(userId)

        val user = withContext(Dispatchers.IO) {
            userRepository.findById(uuid)
        }
        return if (user.isPresent)
            user.get().groups
        else
            setOf()
    }

    override suspend fun findUserGroupsByUsername(username: String): Set<Group> {
        val user = withContext(Dispatchers.IO) {
            userRepository.findByUsername(username)
        }
        return if (user.isPresent)
            user.get().groups
        else
            setOf()
    }

    override suspend fun findUserGroup(userId: String, groupId: String): Group {
        val userUUID = parseUuid(userId)

        val user = withContext(Dispatchers.IO) {
            userRepository
                .findById(userUUID)
                .orElseThrow {
                    log.debug("Group with id $groupId not found for userId $userId")
                    ResourceNotFoundException(
                        "Group with id $groupId not found. Make sure you are a member of this group."
                    )
                }
        }

        val groupUUID = parseUuid(groupId)

        return user.groups.find { it.id == groupUUID }
            ?: run {
                log.debug("Group with id $groupId not found for userId $userId")
                throw ResourceNotFoundException(
                    "Group with id $groupId not found. Make sure you are a member of this group."
                )
            }
    }

    override suspend fun updateUser(userId: String, username: String) {
        val uuid = parseUuid(userId)
        withContext(Dispatchers.IO) {
            val user = userRepository.findById(uuid)
            if (user.isPresent) {
                val userEntity = user.get()
                userEntity.username = username
                try {
                    userRepository.save(userEntity)
                } catch (e: Exception) {
                    log.error("Exception when updating user $userId", e)
                    throw ServiceException("Exception when updating user $userId", e)
                }
            }
        }
    }

    override suspend fun deleteUser(userId: String) {
        val uuid = parseUuid(userId)
        val groups = findUserGroupsById(userId)

        log.info("user $userId is a memeber of ${groups.size} groups")

        withContext(Dispatchers.IO) {
            // elect a new owner for the deleted user's groups
            groups
                .forEach { group ->
                    if (group.ownerId != userId) {
                        // simply remove the user from the group
                        group.users.removeIf { it.id == uuid }
                        groupRepository.save(group)
                        return@forEach
                    }

                    // elect a new owner
                    val newOwner = group.users.find { it.id != uuid }
                    if (newOwner == null) {
                        groupRepository.deleteById(group.id!!)
                        return@forEach
                    }
                    val newOwnerId = newOwner.id.toString()
                    group.ownerId = newOwnerId
                    groupRepository.save(group)
                    userNotificationProducer.sendWithResultLogging(
                        UserNotification(
                            UserNotification.Type.SUCCESS,
                            "Info",
                            "You have been appointed to the owner of group ${group.name}, because the owner has been deleted.",
                            userId = newOwnerId,
                            username = newOwner.username,
                            groupId = group.id.toString(),
                            groupName = group.name
                        )
                    )
                }
        }
    }
}