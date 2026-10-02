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
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/reviews/**").permitAll()
                        // Review mutations resolve the user from Spring Security's Principal.
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reviews/image").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/*").authenticated()
                        .anyRequest().denyAll())
                .build();
    }
}
