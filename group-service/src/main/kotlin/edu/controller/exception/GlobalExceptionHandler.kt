package edu.controller.exception

import edu.service.ResourceNotFoundException
import edu.service.exception.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handle(e: ResourceNotFoundException): ResponseEntity<WebException> {
        val webException = WebException(
            "Resource not found",
            e.message
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(webException)
    }

    @ExceptionHandler(ValidationException::class)
    fun handle(e: ValidationException): ResponseEntity<WebException> {
        val webException = WebException(
            "Validation error",
            e.message
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(webException)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handle(e: ForbiddenException): ResponseEntity<WebException> {
        val webException = WebException(
            "Forbidden",
            e.message
        )
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(webException)
    }

    @ExceptionHandler(Exception::class)
    fun handle(e: Exception): ResponseEntity<WebException> {
        val webException = WebException(
            "Internal server error",
            "An error occurred."
        )
        e.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(webException)
    }
}