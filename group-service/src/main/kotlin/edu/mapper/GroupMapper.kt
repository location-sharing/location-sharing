package edu.mapper

import edu.dto.GroupCreateDto
import edu.dto.GroupDto
import edu.dto.GroupUpdateDto
import edu.repository.model.Group
import java.util.*

object GroupMapper {

    fun from(entity: Group): GroupDto {
        return GroupDto(
            entity.id.toString(),
            entity.name
        )
    }

    fun from(entities: Set<Group>): List<GroupDto> {
        return entities.map(GroupMapper::from)
    }

    fun from(dto: GroupCreateDto): Group {
        return Group(
            name = dto.name
        )
    }

    fun from(id: String, dto: GroupUpdateDto): Group {
        return Group(
            UUID.fromString(id),
            name = dto.name
        )
    }
}