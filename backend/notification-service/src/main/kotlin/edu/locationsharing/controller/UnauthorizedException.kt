package edu.locationsharing.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED, reason = "Token parameter not present, token invalid or expired.")
class UnauthorizedException: Throwable()