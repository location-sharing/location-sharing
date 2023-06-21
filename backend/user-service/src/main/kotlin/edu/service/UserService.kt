package edu.service

import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.dto.user.UserUpdateDto
import edu.location.sharing.events.notifications.SystemNotification
import edu.location.sharing.events.notifications.UserNotification
import edu.mapper.UserMapper
import edu.messaging.producers.SystemNotificationProducer
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
    val systemNotificationProducer: SystemNotificationProducer,
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

    suspend fun findByUsername(username: String): UserDto {
        val user = withContext(Dispatchers.IO) {
            userRepository
                .findByUsername(username)
                .orElseThrow { ResourceNotFoundException("User with username $username not found") }
        }

        return UserMapper.from(user)
    }

    suspend fun register(createDto: UserCreateDto): UserDto {

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

        return withContext(Dispatchers.IO) {
            val updatedUser = userRepository.save(user)
            // system notification so dependencies update user-related data (group-service, etc.)
            systemNotificationProducer.sendWithResultLogging(
                SystemNotification(
                    SystemNotification.Type.USER_UPDATE,
                    userId = id,
                    username = updatedUser.username
                )
            )
            UserMapper.from(updatedUser)
        }
    }

    suspend fun delete(id: String) {
        val uuid = UUID.fromString(id)
        withContext(Dispatchers.IO) {
            userRepository.deleteById(uuid)
            // system notification so dependencies remove user-related data (group-service, etc.)
            systemNotificationProducer.sendWithResultLogging(
                SystemNotification(
                    SystemNotification.Type.USER_DELETE,
                    userId = id
                )
            )
        }
    }
}