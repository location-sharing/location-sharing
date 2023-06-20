package edu.mapper

import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.model.User

object UserMapper {

    fun from(user: User): UserDto {
        return UserDto(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
        )
    }

    fun from(createDto: UserCreateDto): User {
        return User(
            id = null,
            username = createDto.username,
            email = createDto.email,
            password = createDto.password
        )
    }

}