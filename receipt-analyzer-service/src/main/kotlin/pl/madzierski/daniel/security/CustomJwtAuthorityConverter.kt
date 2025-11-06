package pl.madzierski.daniel.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class CustomJwtAuthorityConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.getClaimAsMap("realm_access")
        val roles = realmAccess["roles"] as? List<*> ?: emptyList<Any>()
        return roles.filterIsInstance<String>().filter { it.endsWith("_ROLE") }.map { SimpleGrantedAuthority(it) }
    }
}