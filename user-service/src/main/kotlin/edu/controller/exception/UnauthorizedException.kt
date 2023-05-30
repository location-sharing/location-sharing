package edu.controller.exception

class UnauthorizedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
): RuntimeException(message, cause)