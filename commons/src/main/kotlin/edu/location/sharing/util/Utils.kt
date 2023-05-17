package edu.location.sharing.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Extension function which gives us a logger for the calling class;
 * This way the compiler can infer the type of "T"
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

val objectMapper = jacksonObjectMapper()