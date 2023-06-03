package edu.security

data class AuthenticationException(
    val title: String = "Authentication failed",
    val detail: String?
) : Throwable()