package com.java700.achain.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Maps JWT claims to authorities (flat "roles" claim or Keycloak realm_access.roles). */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoles(jwt).stream()
                .filter(r -> r != null && !r.isBlank())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim().toUpperCase()))
                .collect(Collectors.toList());
        String name = jwt.getClaimAsString("preferred_username");
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("sub");
        }
        return new JwtAuthenticationToken(jwt, authorities, name);
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        Map<String, Object> realm = jwt.getClaim("realm_access");
        if (realm != null && realm.get("roles") instanceof List<?> list) {
            return (List<String>) list;
        }
        return new ArrayList<>();
    }
}
