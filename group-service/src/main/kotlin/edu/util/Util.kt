package edu.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.service.exception.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.qualifiedName)
}

val objectMapper = jacksonObjectMapper()

val log: Logger = LoggerFactory.getLogger("Utils")
fun parseUuid(id: String): UUID {
    return try {
        UUID.fromString(id)
    } catch (e: IllegalArgumentException) {
        log.debug("$id is not a valid UUID")
        throw ValidationException("$id is not a valid UUID")
    }
}