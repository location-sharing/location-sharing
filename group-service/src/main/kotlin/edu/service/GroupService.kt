package edu.service

import edu.dto.GroupCreateDto
import edu.dto.GroupUpdateDto
import edu.mapper.GroupMapper
import edu.repository.GroupRepository
import edu.repository.model.Group
import edu.repository.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Bean
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionManager
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class GroupService(
    val groupRepository: GroupRepository
) {
    
    suspend fun findById(id: String): Group {

        val uuid = UUID.fromString(id)

        return withContext(Dispatchers.IO) {
            groupRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found") }
        }
    }

    suspend fun create(dto: GroupCreateDto): Group {
        val entity = GroupMapper.from(dto)
        return withContext(Dispatchers.IO) {
            groupRepository.save(entity)
        }
    }

    suspend fun patch(id: String, dto: GroupUpdateDto): Group {
        val entity = GroupMapper.from(id, dto)

        withContext(Dispatchers.IO) {
            if (!groupRepository.existsById(entity.id!!)) {
                throw ResourceNotFoundException("Group with id $id not found")
            }
        }

        return withContext(Dispatchers.IO) {
            groupRepository.save(entity)
        }
    }

    suspend fun delete(id: String) {
        val uuid = UUID.fromString(id)
        withContext(Dispatchers.IO) {
            groupRepository.deleteById(uuid)
        }
    }

    suspend fun getGroupUsers(groupId: String): Set<User> {
        val group = findById(groupId)
        return group.users
    }

    suspend fun addGroupUser(groupId: String, userId: String): Group {

        // TODO: send a user validation event before
        val group = findById(groupId)

        if (group.users.size >= 20) {
            throw Exception("Groups can't have more than 20 users")
        }

        // we don't explicitly get the user and then add it to the set, we just set the id,
        // and cascading takes care of the rest
        val userToAdd = User()
        userToAdd.id = UUID.fromString(userId)
        userToAdd.name = "random-name-$userId"

        group.users.add(userToAdd)

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun removeGroupUser(groupId: String, userId: String) {

        val group = findById(groupId)

        // we don't explicitly get the user and then remove, we just remove the id,
        // and cascading takes care of the rest
        group.users.removeIf { it.id == UUID.fromString(userId) }
        withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }
}