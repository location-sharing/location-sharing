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
            userRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("User with id $userId not found") }
        }
        return user.groups
    }
}