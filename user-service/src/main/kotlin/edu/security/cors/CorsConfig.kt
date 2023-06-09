package edu.security.cors

import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Component
class CorsConfig(
    val corsConfigMappings: CorsConfigMappings
) {

    // builds the cors config from the values given in application.yaml
    val configurationSource: CorsConfigurationSource by lazy {
        val source = UrlBasedCorsConfigurationSource()
        corsConfigMappings.mappings.forEach { mapping ->
            val config = CorsConfiguration().apply {
                allowedOriginPatterns = mapping.originPatterns.toList()
                allowedMethods = mapping.allowedMethods.toList()
                allowedHeaders = mapping.allowedHeaders.toList()
                exposedHeaders = mapping.exposedHeaders.toList()
                allowCredentials = mapping.allowCredentials
                maxAge = mapping.maxAge
            }
            source.registerCorsConfiguration(mapping.url, config)
        }
        source
    }
}