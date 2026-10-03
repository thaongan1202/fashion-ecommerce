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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/verify-email",
                                "/api/auth/password-reset/request",
                                "/api/auth/password-reset/confirm",
                                "/api/auth/login").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews/image").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/*").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/cart", "/api/cart/**", "/api/orders", "/api/orders/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(
                                "/api/users/me/addresses",
                                "/api/users/me/addresses/**"
                        ).hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
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
