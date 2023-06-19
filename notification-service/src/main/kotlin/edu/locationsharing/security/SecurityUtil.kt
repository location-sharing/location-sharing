package edu.locationsharing.security

import edu.locationsharing.security.jwt.AuthenticatedUser
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {
    fun getAuthenticatedUser(): AuthenticatedUser {
        return SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
    }
}