package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.response.order.OrderReturnResponse;
import com.utephonehub.backend.dto.response.order.ReturnStatisticsResponse;
import com.utephonehub.backend.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IOrderReturnService {

    OrderReturnResponse createReturn(Long userId, Long orderId, String reason, MultipartFile evidence);

    OrderReturnResponse getMyReturn(Long userId, Long orderId);

    Page<OrderReturnResponse> getReturns(ReturnStatus status, Pageable pageable);

    ReturnStatisticsResponse getStatistics();

    OrderReturnResponse approve(Long returnId);

    OrderReturnResponse reject(Long returnId, String adminNote);
}
