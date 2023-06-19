package edu.service.exception

class ServiceException(
    override val message: String,
    override val cause: Throwable? = null
): Throwable()