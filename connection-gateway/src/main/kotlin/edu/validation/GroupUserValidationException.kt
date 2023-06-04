package edu.validation

data class GroupUserValidationException(
    override val message: String
) : Throwable(message)