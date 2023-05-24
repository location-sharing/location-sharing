package edu.mapper

import edu.dto.GroupCreateDto
import edu.dto.GroupDto
import edu.model.Group

object GroupMapper {

    fun from(group: Group): GroupDto {
        return GroupDto(group.id.toString(), group.name)
    }

    fun from(createDto: GroupCreateDto): Group {
        return Group(null, createDto.name)
    }

}