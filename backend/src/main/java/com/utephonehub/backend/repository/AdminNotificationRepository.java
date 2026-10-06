package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findTop30ByOrderByCreatedAtDesc();

    long countByReadFalse();

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("UPDATE AdminNotification n SET n.read = true WHERE n.read = false")
    int markAllRead();
}
