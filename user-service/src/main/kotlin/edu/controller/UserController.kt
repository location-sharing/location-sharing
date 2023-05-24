package edu.controller

import edu.dto.UserCreateDto
import edu.dto.UserDto
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
        val userDto = userService.findById(id)
        return ResponseEntity.ok(userDto)
    }
}