package edu.service

import edu.dto.GroupCreateDto
import edu.dto.GroupUpdateDto
import edu.location.sharing.models.events.validation.user.*
import edu.mapper.GroupMapper
import edu.mapper.UserMapper
import edu.messaging.producers.UserValidationRequestProducer
import edu.repository.GroupRepository
import edu.repository.model.Group
import edu.repository.model.User
import edu.service.exception.ValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(
    val groupRepository: GroupRepository,
    val userValidationRequestProducer: UserValidationRequestProducer
) {

    suspend fun create(dto: GroupCreateDto, ownerId: String): Group {
        val entity = GroupMapper.toEntity(dto, ownerId)
        val group = withContext(Dispatchers.IO) {
            groupRepository.save(entity)
        }

        // add the owner of the group as a member (with the same flow as adding normal users)
        addGroupUser(group.id.toString(), ownerId = ownerId, userId = ownerId)
        return group
    }

    suspend fun patch(id: String, dto: GroupUpdateDto, ownerId: String): Group {
        verifyGroupExists(id, ownerId)
        val entity = GroupMapper.toEntity(id, dto, ownerId)
        return withContext(Dispatchers.IO) {
            groupRepository.save(entity)
        }
    }

    suspend fun delete(id: String, ownerId: String) {
        val uuid = UUID.fromString(id)
        withContext(Dispatchers.IO) {
            groupRepository.deleteByIdAndOwnerId(uuid, ownerId)
        }
    }

    /**
     * Sends out a validation event to fetch data about the user we're trying to add
     */
    suspend fun addGroupUser(groupId: String, ownerId: String, userId: String) {
        val group = findByIdAndOwner(groupId, ownerId)

        if (group.users.size >= 20) {
            throw ValidationException("Groups can't have more than 20 users")
        }

        val metadata = UserValidationMetadata(
            initiatorUserId = ownerId,
            purpose = UserValidationPurpose.GROUP_ADD_USER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(userId, metadata)
        )
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    suspend fun addGroupUserFromEvent(groupId: String, ownerId: String, userEvent: UserEvent): Group {
        val group = findByIdAndOwner(groupId, ownerId)

        val userToAdd = UserMapper.from(userEvent)

        // only add to the set, let cascading do the rest
        group.users.add(userToAdd)

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun removeGroupUser(groupId: String, ownerId: String, userId: String) {
        // TODO: allow to remove yourself from a group
        val group = findByIdAndOwner(groupId, ownerId)

        if (group.ownerId == userId) {
            throw ValidationException("You can't remove the owner of the group")
        }

        // only remove from the set, let cascading do the rest
        group.users.removeIf { it.id == UUID.fromString(userId) }
        withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    suspend fun changeOwner(groupId: String, currentOwnerId: String, newOwnerId: String) {
        if (currentOwnerId == newOwnerId) {
            return
        }
        verifyGroupExists(groupId, currentOwnerId)
        val metadata = UserValidationMetadata(
            initiatorUserId = currentOwnerId,
            purpose = UserValidationPurpose.GROUP_CHANGE_OWNER,
            mapOf(
                AdditionalInfoKey.GROUP_ID to groupId
            )
        )
        userValidationRequestProducer.sendWithResultLogging(
            UserValidationRequestEvent(newOwnerId, metadata)
        )
    }

    /**
     * Gets invoked once a user validation request comes in
     */
    suspend fun changeOwnerFromEvent(groupId: String, currentOwnerId: String, userEvent: UserEvent) {
        val group = findByIdAndOwner(groupId, currentOwnerId)

        group.ownerId = userEvent.id

        // if the new owner is not in the group, add it
        val newOwnerUUID = UUID.fromString(userEvent.id)
        if (group.users.find { it.id == newOwnerUUID } == null) {
            group.users.add(UserMapper.from(userEvent))
        }

        return withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }
    }

    private suspend fun findByIdAndOwner(id: String, ownerId: String): Group {
        val groupUUID = UUID.fromString(id)

        return withContext(Dispatchers.IO) {
            groupRepository
                .findByIdAndOwnerId(groupUUID, ownerId)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found for user $ownerId") }
        }
    }

    private suspend fun verifyGroupExists(groupId: String, ownerId: String) {
        withContext(Dispatchers.IO) {
            if (!groupRepository.existsByIdAndOwnerId(UUID.fromString(groupId), ownerId)) {
                throw ResourceNotFoundException("Group with id $groupId not found for user $ownerId")
            }
        }
    }
}