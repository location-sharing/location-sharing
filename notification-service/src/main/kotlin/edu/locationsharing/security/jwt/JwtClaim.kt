package edu.locationsharing.security.jwt

enum class JwtClaim(
    val keyName: String,
) {
    USER_ID("userId"),
    USER_NAME("username"),
}