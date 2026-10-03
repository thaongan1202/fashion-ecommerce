package vn.edu.hcmute.fashion.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock private ReviewRepository repository;
    @Mock private ReviewImageStorageService imageStorage;
    @InjectMocks private ReviewService service;

    @Test
    void customerCannotReviewProductWithoutDeliveredPurchase() {
        when(repository.productExists(20L)).thenReturn(true);
        when(repository.hasDeliveredVariant(7L, 20L, 100L)).thenReturn(false);

        var error = assertThrows(ResponseStatusException.class,
                () -> service.create(20L, 7L, 5, "Chất vải đẹp", 100L, 170, new BigDecimal("65"), null));

        assertEquals(403, error.getStatusCode().value());
        verify(repository, never()).create(7L, 20L, 5, "Chất vải đẹp", "APPROVED", 100L, 170, new BigDecimal("65"), null);
    }

    @Test
    void customerCannotCreateSecondReviewForSameProduct() {
        when(repository.productExists(20L)).thenReturn(true);
        when(repository.hasDeliveredVariant(7L, 20L, 100L)).thenReturn(true);
        when(repository.alreadyReviewed(7L, 20L)).thenReturn(true);

        var error = assertThrows(ResponseStatusException.class,
                () -> service.create(20L, 7L, 4, "Nội dung mới", 100L, null, null, null));

        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).create(7L, 20L, 4, "Nội dung mới", "APPROVED", 100L, null, null, null);
    }

    @Test
    void ownerCanEditExistingReviewAndItReturnsToPending() {
        var updated = new ReviewResponse(12L, "Demo Customer", 4, "Đã cập nhật", null, "PENDING",
                100L, "M", "Đen", 170, new BigDecimal("65"), OffsetDateTime.now());
        when(repository.productExists(20L)).thenReturn(true);
        when(repository.hasDeliveredVariant(7L, 20L, 100L)).thenReturn(true);
        when(repository.update(12L, 7L, 20L, 4, "Đã cập nhật", "PENDING", 100L, 170, new BigDecimal("65"), null)).thenReturn(updated);

        var result = service.update(20L, 12L, 7L, 4, "Đã cập nhật", 100L, 170, new BigDecimal("65"), null);

        assertEquals("PENDING", result.status());
        assertEquals("Đã cập nhật", result.comment());
    }

    @Test
    void customerCannotEditAnotherCustomersReview() {
        when(repository.productExists(20L)).thenReturn(true);
        when(repository.hasDeliveredVariant(8L, 20L, 100L)).thenReturn(true);
        when(repository.update(12L, 8L, 20L, 4, "Nội dung giả mạo", "PENDING", 100L, null, null, null)).thenReturn(null);

        var error = assertThrows(ResponseStatusException.class,
                () -> service.update(20L, 12L, 8L, 4, "Nội dung giả mạo", 100L, null, null, null));

        assertEquals(404, error.getStatusCode().value());
        verify(repository).update(12L, 8L, 20L, 4, "Nội dung giả mạo", "PENDING", 100L, null, null, null);
    }

    @Test
    void customerReviewWaitsForAdminApproval() {
        var pending = new ReviewResponse(14L, "Demo Customer", 5, "Sản phẩm rất đẹp", null, "PENDING", 100L, "M", "Đen", null, null, OffsetDateTime.now());
        when(repository.productExists(20L)).thenReturn(true);
        when(repository.hasDeliveredVariant(7L, 20L, 100L)).thenReturn(true);
        when(repository.alreadyReviewed(7L, 20L)).thenReturn(false);
        when(repository.create(7L, 20L, 5, "Sản phẩm rất đẹp", "PENDING", 100L, null, null, null)).thenReturn(pending);

        var result = service.create(20L, 7L, 5, "Sản phẩm rất đẹp", 100L, null, null, null);

        assertEquals("PENDING", result.status());
    }
}
