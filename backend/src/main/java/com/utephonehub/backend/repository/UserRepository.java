package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    @Query(value = """
            SELECT * FROM users u
            WHERE CASE
                WHEN lower(split_part(trim(u.email), '@', 2)) IN ('gmail.com', 'googlemail.com')
                    THEN regexp_replace(
                        regexp_replace(lower(split_part(trim(u.email), '@', 1)), '\\+.*$', ''),
                        '\\.', '', 'g'
                    ) || '@gmail.com'
                ELSE lower(trim(u.email))
            END = :email
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findByCanonicalEmail(@Param("email") String email);
    Optional<User> findByUsername(String username);

    List<User> findByRoleAndDeletedAtIsNull(UserRole role);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("UPDATE User u SET u.walletBalance = COALESCE(u.walletBalance, 0) + :amount WHERE u.id = :userId")
    int creditWallet(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    /**
     * Find users created between start and end date/time
     * Used for user registration chart
     * 
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return List of users created in the date range
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}

