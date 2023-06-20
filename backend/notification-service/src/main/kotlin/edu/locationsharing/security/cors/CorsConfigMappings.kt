package edu.locationsharing.security.cors

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("cors")
class CorsConfigMappings(
    val mappings: List<CorsMapping>
) {
    class CorsMapping(
        val url: String,
        val originPatterns: List<String>,
        val allowedMethods: List<String>,
        val allowedHeaders: List<String>,
        val exposedHeaders: List<String>,
        val allowCredentials: Boolean = false,
        val maxAge: Long = 1800 // spring default, 30min (in seconds)
    )
}