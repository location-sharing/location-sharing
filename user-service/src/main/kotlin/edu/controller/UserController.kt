package edu.controller

import edu.dto.login.AuthToken
import edu.dto.login.LoginCredentials
import edu.dto.user.UserCreateDto
import edu.dto.user.UserDto
import edu.dto.user.UserUpdateDto
import edu.security.AuthenticationService
import edu.security.jwt.AuthenticatedUser
import edu.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
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
    suspend fun authenticate(@RequestBody loginCredentials: LoginCredentials): ResponseEntity<AuthToken> {
        val jwt = authService.authenticate(loginCredentials)
        return ResponseEntity.ok(AuthToken(jwt))
    }

    @GetMapping
    suspend fun getUser(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<UserDto> {
        val user = userService.findById(authenticatedUser.id)
        return ResponseEntity.ok(user)
    }

    @PatchMapping
    suspend fun updateUser(
        @RequestBody updateDto: UserUpdateDto,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<UserDto> {
        val user = userService.patch(authenticatedUser.id, updateDto)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping
    suspend fun deleteUser(@AuthenticationPrincipal authenticatedUser: AuthenticatedUser): ResponseEntity<Void> {
        userService.delete(authenticatedUser.id)
        return ResponseEntity.noContent().build()
    }
}