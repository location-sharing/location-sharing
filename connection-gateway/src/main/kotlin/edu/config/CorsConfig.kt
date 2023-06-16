package edu.config

import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.plugins.cors.*
import java.util.regex.Pattern

object CorsConfig {

    private val config = ApplicationConfig("application.yaml")

    private val corsOptions = config.config("cors").toMap()

    private val originPatternConfig = corsOptions["originPattern"] as String
    private val allowedMethodsConfig = corsOptions["allowedMethodPattern"] as List<Any?>?
    private val allowedHeadersConfig = corsOptions["allowedHeaders"] as List<Any?>?
    private val exposedHeadersConfig = corsOptions["exposedHeaders"] as List<Any?>?

    private val allowCredentialsConfig = (corsOptions["allowCredentials"] as String? ?: "false").toBoolean()
    private val maxAgeConfig = (corsOptions["maxAge"] as String? ?: "1800").toLong()

    fun installCors(config: CORSConfig) {
        val originMatcher = Pattern.compile(originPatternConfig, Pattern.CASE_INSENSITIVE).asMatchPredicate()
        config.allowOrigins { originMatcher.test(it) }

        allowedMethodsConfig?.forEach { config.allowMethod(HttpMethod(it as String)) }

        val headerMatchers = allowedHeadersConfig?.map { Pattern.compile(it as String).asMatchPredicate() }
        config.allowHeaders {header ->
            try {
                headerMatchers?.first { matcher -> matcher.test(header) }
                true
            } catch (e: NoSuchElementException) {
                false
            }
        }

        exposedHeadersConfig?.forEach { config.exposeHeader(it as String) }

        config.allowCredentials = allowCredentialsConfig
        config.maxAgeInSeconds = maxAgeConfig
    }
}