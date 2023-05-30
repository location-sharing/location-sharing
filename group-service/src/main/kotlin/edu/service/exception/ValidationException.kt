package edu.service.exception

class ValidationException(
    override val message: String? = null,
    override val cause: Throwable? = null
): RuntimeException(message, cause)