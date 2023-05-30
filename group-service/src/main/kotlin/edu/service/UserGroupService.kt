package edu.service

import edu.repository.UserRepository
import edu.repository.model.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserGroupService(
    private val userRepository: UserRepository
) {
    suspend fun findUserGroups(userId: String): Set<Group> {
        val uuid = UUID.fromString(userId)
        val user = withContext(Dispatchers.IO) {
            userRepository.findById(uuid)
        }

        return if (user.isPresent)
            user.get().groups
        else
            setOf()
    }

    suspend fun findUserGroup(userId: String, groupId: String): Group {
        val userUUID = UUID.fromString(userId)

        val user = withContext(Dispatchers.IO) {
            userRepository
                .findById(userUUID)
                .orElseThrow {
                    ResourceNotFoundException("Group with id $groupId not found for user")
                }
        }

        val groupUUID = UUID.fromString(groupId)

        return user.groups.find { it.id == groupUUID }
            ?: throw ResourceNotFoundException("Group with id $groupId not found for user")
    }
}