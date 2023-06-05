package edu.mapper

import edu.dto.UserDto
import edu.location.sharing.events.validation.user.UserEvent
import edu.repository.model.User
import java.util.*

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

    fun from(userEvent: UserEvent): User {
        return User(
            UUID.fromString(userEvent.id),
            userEvent.name
        )
    }
}