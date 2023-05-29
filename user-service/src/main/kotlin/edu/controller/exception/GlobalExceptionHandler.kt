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

    @ExceptionHandler(Exception::class)
    fun handle(e: Exception): ResponseEntity<WebException> {
        val webException = WebException(
            "Exception",
            e.message
        )
        return ResponseEntity
            .status(500)
            .body(webException)
    }
}