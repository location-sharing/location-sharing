package edu.mapper

import edu.dto.GroupCreateDto
import edu.dto.GroupDetailDto
import edu.dto.GroupDto
import edu.dto.GroupPatchDto
import edu.model.Group

object GroupMapper {

    fun toDto(entity: Group): GroupDto {
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
        return entities.map(GroupMapper::toDto)
    }

    fun toEntity(dto: GroupCreateDto, ownerId: String): Group {
        return Group(
            name = dto.name,
            ownerId = ownerId,
        )
    }

    fun patchEntity(group: Group, dto: GroupPatchDto): Group {
        return Group(
            group.id,
            dto.name,
            group.ownerId,
            group.users
        )
    }
}