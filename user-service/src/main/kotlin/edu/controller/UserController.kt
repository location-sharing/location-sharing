package edu.controller

import edu.dto.login.LoginCredentials
import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.dto.user.UserUpdateDto
import edu.security.AuthenticationService
import edu.service.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    val userService: UserService,
    val authService: AuthenticationService,
) {

    @PostMapping
    suspend fun postUser(@RequestBody createUser: UserCreateDto): ResponseEntity<UserDto> {
        val user = userService.register(createUser)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/authenticate")
    suspend fun login(@RequestBody loginCredentials: LoginCredentials): ResponseEntity<Void> {
        val jwt = authService.authenticate(loginCredentials)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
            .build()
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