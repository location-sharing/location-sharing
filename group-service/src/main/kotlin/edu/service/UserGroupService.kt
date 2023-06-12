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
    suspend fun findUserGroupsById(userId: String): Set<Group> {
        val uuid = UUID.fromString(userId)
        val user = withContext(Dispatchers.IO) {
            userRepository.findById(uuid)
        }

        return if (user.isPresent)
            user.get().groups
        else
            setOf()
    }

    suspend fun findUserGroupsByUsername(username: String): Set<Group> {
        val user = withContext(Dispatchers.IO) {
            userRepository.findByUsername(username)
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
                    ResourceNotFoundException(
                        "Group with id $groupId not found. Make sure you are a member of this group."
                    )
                }
        }

        val groupUUID = try {
            UUID.fromString(groupId)
        } catch (e: Exception) {
            throw ResourceNotFoundException(
                "Group id $groupId is not a valid UUID"
            )
        }

        return user.groups.find { it.id == groupUUID }
            ?: throw ResourceNotFoundException(
                "Group with id $groupId not found. Make sure you are a member of this group."
            )
    }
}