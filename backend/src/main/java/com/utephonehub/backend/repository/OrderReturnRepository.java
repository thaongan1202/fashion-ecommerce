package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.OrderReturn;
import com.utephonehub.backend.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

    Optional<OrderReturn> findByOrderId(Long orderId);

    Page<OrderReturn> findByStatusOrderByCreatedAtDesc(ReturnStatus status, Pageable pageable);

    Page<OrderReturn> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(ReturnStatus status);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM OrderReturn r WHERE r.status = com.utephonehub.backend.enums.ReturnStatus.APPROVED")
    BigDecimal sumApprovedRefundAmount();

    List<OrderReturn> findByStatusAndReviewedAtBetween(ReturnStatus status, LocalDateTime start, LocalDateTime end);
}
