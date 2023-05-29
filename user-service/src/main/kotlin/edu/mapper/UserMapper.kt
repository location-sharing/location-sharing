package edu.mapper

import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.model.User

object UserMapper {

    fun from(user: User): UserDto {
        return UserDto(
            user.id.toString(),
            user.username,
            user.email,
        )
    }

    fun from(createDto: UserCreateDto): User {
        return User(
            null,
            createDto.username,
            createDto.email,
            createDto.password
        )
    }

}