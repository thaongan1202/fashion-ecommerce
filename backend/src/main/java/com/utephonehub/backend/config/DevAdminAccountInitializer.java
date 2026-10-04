package com.utephonehub.backend.config;

import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.EGender;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevAdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.test-admin.username:testadmin}")
    private String username;

    @Value("${app.test-admin.email:testadmin@utefashionhub.local}")
    private String email;

    @Value("${app.test-admin.password:Admin123!}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            log.info("Development admin account already exists; skipping seed.");
            return;
        }

        User testAdmin = User.builder()
                .username(username)
                .fullName("Test Admin")
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .phoneNumber("0900000000")
                .gender(EGender.OTHER)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(testAdmin);
        log.info("Created development admin account '{}'.", username);
    }
}
