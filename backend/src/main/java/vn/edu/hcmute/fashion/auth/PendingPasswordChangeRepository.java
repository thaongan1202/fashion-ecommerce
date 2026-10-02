package vn.edu.hcmute.fashion.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingPasswordChangeRepository
        extends JpaRepository<PendingPasswordChange, Long> {
}