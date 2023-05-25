package edu.mapper

import edu.dto.UserDto
import edu.repository.model.User

object UserMapper {

    private fun from(entity: User): UserDto {
        return UserDto(
            entity.id.toString(),
            entity.name
        )
    }

    fun from(entities: Set<User>): List<UserDto> {
        return entities.map(UserMapper::from)
    }
}