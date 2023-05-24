package edu.mapper

import edu.dto.UserCreateDto
import edu.dto.UserDto
import edu.dto.UserUpdateDto
import edu.model.User

object UserMapper {

    fun from(user: User): UserDto {
        return UserDto(user.id.toString(), user.username)
    }

    fun from(createDto: UserCreateDto): User {
        return User(null, createDto.username, createDto.password)
    }

}