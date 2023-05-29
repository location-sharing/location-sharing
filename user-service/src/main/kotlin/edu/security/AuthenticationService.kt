package edu.security

import edu.dto.login.LoginCredentials
import edu.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
) {

    fun authenticate(loginCredentials: LoginCredentials): String {
        val username = loginCredentials.username
        val password = loginCredentials.password

        if (username.isBlank()) {
            throw BadCredentialsException("Username cannot be empty.")
        }

        if (password.isBlank()) {
            throw BadCredentialsException("Password cannot be empty.")
        }

        val user = userRepository.findByUsername(username)
            .orElseThrow {
                BadCredentialsException("User with username $username does not exist.")
            }

        if (!passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException("Invalid password.")
        }

        return jwtUtils.generate(user)
    }


}