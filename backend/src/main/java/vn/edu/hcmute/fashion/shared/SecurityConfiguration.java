package vn.edu.hcmute.fashion.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/demo/**").permitAll()
                        // Admin routes verify the active ADMIN role from the server-side session.
                        .requestMatchers("/api/admin/reviews/**").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/reviews/**").permitAll()
                        // The demo review controller verifies its server-side HttpSession.
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews/image").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/*").permitAll()
                        .anyRequest().denyAll())
                .build();
    }
}
