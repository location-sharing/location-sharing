package edu.service

import edu.dto.GroupCreateDto
import edu.dto.GroupDto
import edu.dto.GroupUpdateDto
import edu.mapper.GroupMapper
import edu.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(
    val groupRepository: GroupRepository
) {
    
    suspend fun findById(id: String): GroupDto {

        val uuid = UUID.fromString(id)

        val group = withContext(Dispatchers.IO) {
            groupRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found") }
        }

        return GroupMapper.from(group)
    }

    suspend fun create(createDto: GroupCreateDto): GroupDto {
        val user = GroupMapper.from(createDto)

        return withContext(Dispatchers.IO) {
            val savedUser = groupRepository.save(user)
            GroupMapper.from(savedUser)
        }
    }

    suspend fun patch(id: String, updateDto: GroupUpdateDto): GroupDto {

        val uuid = UUID.fromString(id)

        val group = withContext(Dispatchers.IO) {
            groupRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("Group with id $id not found") }
        }

        group.name = updateDto.name

        val updatedUser = withContext(Dispatchers.IO) {
            groupRepository.save(group)
        }

        return GroupMapper.from(updatedUser)
    }

    suspend fun delete(id: String) {
        val uuid = UUID.fromString(id)
        withContext(Dispatchers.IO) {
            groupRepository.deleteById(uuid)
        }
    }
}