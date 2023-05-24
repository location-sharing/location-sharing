package edu.controller

import edu.dto.UserCreateDto
import edu.dto.UserDto
import edu.dto.UserUpdateDto
import edu.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    val userService: UserService
) {

    @PostMapping
    suspend fun postUser(@RequestBody createUser: UserCreateDto): ResponseEntity<UserDto> {
        val user = userService.register(createUser)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/{id}")
    suspend fun getUser(@PathVariable id: String): ResponseEntity<UserDto> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/{id}")
    suspend fun updateUser(
        @PathVariable id: String,
        @RequestBody updateDto: UserUpdateDto
    ): ResponseEntity<UserDto> {
        val user = userService.patch(id, updateDto)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteUser(@PathVariable id: String) {
        userService.delete(id)
    }
}