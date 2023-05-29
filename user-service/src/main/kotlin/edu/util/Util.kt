package edu.util

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.impl.JWTParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.qualifiedName)
}

val objectMapper = jacksonObjectMapper()