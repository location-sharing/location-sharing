package edu.mapper

import edu.dto.GroupCreateDto
import edu.dto.GroupDetailDto
import edu.dto.GroupDto
import edu.dto.GroupUpdateDto
import edu.repository.model.Group
import java.util.*

object GroupMapper {

    fun toEntity(entity: Group): GroupDto {
        return GroupDto(
            entity.id.toString(),
            entity.name,
            entity.ownerId,
        )
    }

    fun toDetailDto(entity: Group): GroupDetailDto {
        return GroupDetailDto(
            entity.id.toString(),
            entity.name,
            entity.ownerId,
            UserMapper.from(entity.users)
        )
    }

    fun toDtoList(entities: Set<Group>): List<GroupDto> {
        return entities.map(GroupMapper::toEntity)
    }

    fun toEntity(dto: GroupCreateDto, ownerId: String): Group {
        return Group(
            name = dto.name,
            ownerId = ownerId,
        )
    }

    fun toEntity(id: String, dto: GroupUpdateDto, ownerId: String): Group {
        return Group(
            UUID.fromString(id),
            dto.name,
            ownerId
        )
    }
}