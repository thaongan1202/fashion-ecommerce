package vn.edu.hcmute.fashion.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingPasswordResetRepository
        extends JpaRepository<PendingPasswordReset, Long> {
}
