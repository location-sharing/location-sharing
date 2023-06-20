package edu.service.exception

class GroupOperationForbiddenException(
    override val message: String? = null,
    override val cause: Throwable? = null,
): RuntimeException(message, cause)