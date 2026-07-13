package pl.madzierski.daniel.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

class CustomJwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream().filter(role -> role.endsWith("_ROLE")).map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());
    }
}

