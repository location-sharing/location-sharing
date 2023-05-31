package edu.controller.exception

class ForbiddenException(
    override val message: String? = null,
    override val cause: Throwable? = null,
): RuntimeException(message, cause)