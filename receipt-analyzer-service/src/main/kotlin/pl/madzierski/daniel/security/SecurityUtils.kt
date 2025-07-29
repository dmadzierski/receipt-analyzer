package pl.madzierski.daniel.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SecurityUtils {

    companion object {
        fun getCurrentUserSub(): String {
            return SecurityContextHolder.getContext().authentication.name
        }

        fun getCurrentUserRoles(): List<String> {
            return SecurityContextHolder.getContext().authentication.authorities.map { it.authority }
        }

        fun getClaim(claim: String): Any? {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication is JwtAuthenticationToken) {
                return authentication.token.getClaimAsString(claim)
            }
            return null
        }
    }
}