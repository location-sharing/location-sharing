package edu.repository

import edu.model.Group
import edu.model.User
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupUserRepository {
    fun getGroupUsers(groupId: UUID): Set<User>
    fun getUserGroups(userId: UUID): Set<Group>
    fun saveGroupUser(groupId: UUID, user: User)
    fun deleteGroupUser(groupId: UUID, user: User)
}