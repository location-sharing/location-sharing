package edu.security

data class AuthException(
    val title: String = "Authentication failed",
    val detail: String
)