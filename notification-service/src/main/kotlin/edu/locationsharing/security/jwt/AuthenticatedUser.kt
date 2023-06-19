package edu.locationsharing.security.jwt

data class AuthenticatedUser(
    val id: String,
    val username: String
)