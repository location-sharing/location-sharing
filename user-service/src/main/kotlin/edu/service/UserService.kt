package edu.service

import edu.dto.UserCreateDto
import edu.dto.UserDto
import edu.mapper.UserMapper
import edu.repository.ResourceNotFoundException
import edu.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    val userRepository: UserRepository
) {
    suspend fun findById(id: String): UserDto {

        val uuid = UUID.fromString(id)

        val handler = CoroutineExceptionHandler { _, error -> throw error }

        val user = withContext(Dispatchers.IO + handler) {
            userRepository
                .findById(uuid)
                .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        }

        return UserMapper.from(user)
    }

    suspend fun register(createDto: UserCreateDto): UserDto {
        // TODO: hash password and stuff

        val user = UserMapper.from(createDto)

        return withContext(Dispatchers.IO) {
            val savedUser = userRepository.save(user)
            UserMapper.from(savedUser)
        }
    }
}