package edu.controller.exception

import edu.service.ResourceNotFoundException
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

    @ExceptionHandler(UnauthorizedException::class)
    fun handle(e: UnauthorizedException): ResponseEntity<WebException> {
        val webException = WebException(
            "Unauthorized",
            e.message
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
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