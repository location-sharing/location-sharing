package edu.repository.impl

import edu.model.User
import edu.repository.GroupRepository
import edu.repository.GroupUserRepository
import edu.service.ResourceNotFoundException
import edu.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class GroupRepositoryImpl(
    val groupRepository: GroupRepository,
    val userRepository: UserRepository,
): GroupUserRepository {

    override fun getGroupUsers(groupId: UUID): Set<User> {
        return groupRepository
            .findById(groupId)
            .orElseThrow { ResourceNotFoundException() }
    }

    override fun getUserGroups(userId: UUID) {
        TODO("Not yet implemented")
    }

    override fun saveGroupUser(groupId: UUID, user: User) {
        TODO("Not yet implemented")
    }

    override fun deleteGroupUser(groupId: UUID, user: User) {
        TODO("Not yet implemented")
    }
}