package edu.service

import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.dto.user.UserUpdateDto
import edu.mapper.UserMapper
import edu.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
) {
    suspend fun findById(id: String): UserDto {

        val uuid = UUID.fromString(id)

        val user = withContext(Dispatchers.IO) {
            userRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        }

        return UserMapper.from(user)
    }

    suspend fun register(createDto: UserCreateDto): UserDto {
        // TODO: hash password and stuff

        val password = passwordEncoder.encode(createDto.password)
        val dto = UserCreateDto(createDto.username, createDto.email, password)

        val user = UserMapper.from(dto)

        return withContext(Dispatchers.IO) {
            val savedUser = userRepository.save(user)
            UserMapper.from(savedUser)
        }
    }

    suspend fun patch(id: String, updateDto: UserUpdateDto): UserDto {

        val uuid = UUID.fromString(id)

        val user = withContext(Dispatchers.IO) {
            userRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        }

        user.username = updateDto.username

        val updatedUser = withContext(Dispatchers.IO) {
            userRepository.save(user)
        }

        return UserMapper.from(updatedUser)
    }

    suspend fun delete(id: String) {
        val uuid = UUID.fromString(id)
        withContext(Dispatchers.IO) {
            userRepository.deleteById(uuid)
        }
    }
}