package vn.edu.hcmute.fashion.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByEmailIgnoreCase(String email);
}
