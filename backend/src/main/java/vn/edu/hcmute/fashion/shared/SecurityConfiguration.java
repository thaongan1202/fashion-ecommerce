package vn.edu.hcmute.fashion.shared;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
                .build();
    }
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }

            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                String normalizedRole = role.toUpperCase(Locale.ROOT);
                if (normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = normalizedRole.substring("ROLE_".length());
                }
                authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
            }

            return authorities;
        });

        return converter;
    }
}
