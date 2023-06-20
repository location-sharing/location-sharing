package edu.service.impl

import edu.model.Group
import edu.repository.UserRepository
import edu.service.UserGroupService
import edu.service.exception.ResourceNotFoundException
import edu.util.logger
import edu.util.parseUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class UserGroupServiceImpl(
    private val userRepository: UserRepository
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
}