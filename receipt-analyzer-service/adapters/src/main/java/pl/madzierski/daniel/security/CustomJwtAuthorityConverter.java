package pl.madzierski.daniel.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class CustomJwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null)
            return Collections.emptyList();
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null)
            return Collections.emptyList();
        return roles.stream()
            .filter(role -> role.endsWith("_ROLE")).map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableSet());
    }
}

