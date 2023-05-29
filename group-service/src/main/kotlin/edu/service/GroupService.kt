package edu.service

import edu.dto.GroupCreateDto
import edu.dto.GroupUpdateDto
import edu.mapper.GroupMapper
import edu.messaging.events.*
import edu.messaging.producers.UserValidationRequestProducer
import edu.repository.GroupRepository
import edu.repository.model.Group
import edu.repository.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(
    val groupRepository: GroupRepository,
    val userValidationRequestProducer: UserValidationRequestProducer
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

    suspend fun addGroupUser(groupId: String, userId: String) {

        val group = findById(groupId)

        if (group.users.size >= 20) {
            throw Exception("Groups can't have more than 20 users")
        }

        UserValidationRequestContainer.addPendingRequest(userId, groupId)
        val metadata = UserValidationMetadata(
            initiatorUserId = "ADMIN",
            purpose = UserValidationPurpose.GROUP_ADD_USER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(userId, metadata)
        )
    }

    suspend fun insertGroupUserFromEvent(groupId: String, userEvent: UserEvent): Group {
        val group = findById(groupId)

        val userToAdd = User()
        userToAdd.id = UUID.fromString(userEvent.id)
        userToAdd.name = userEvent.name

        // only add to the set, let cascading to the rest
        group.users.add(userToAdd)

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun removeGroupUser(groupId: String, userId: String) {

        val group = findById(groupId)

        // only remove from the set, let cascading to the rest
        group.users.removeIf { it.id == UUID.fromString(userId) }
        withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }
}