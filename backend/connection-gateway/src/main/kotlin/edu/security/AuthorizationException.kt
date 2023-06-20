package edu.security

data class AuthorizationException(
    val title: String = "Forbidden",
    val detail: String?
) : Throwable()