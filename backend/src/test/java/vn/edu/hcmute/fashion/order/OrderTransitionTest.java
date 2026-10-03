package vn.edu.hcmute.fashion.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderTransitionTest {
    @Test
    void allowsOnlyForwardFulfillmentAndEligibleCancellation() {
        assertTrue(OrderService.isAllowedTransition("PENDING", "PROCESSING"));
        assertTrue(OrderService.isAllowedTransition("PENDING", "CANCELLED"));
        assertTrue(OrderService.isAllowedTransition("PROCESSING", "SHIPPING"));
        assertTrue(OrderService.isAllowedTransition("PROCESSING", "CANCELLED"));
        assertTrue(OrderService.isAllowedTransition("SHIPPING", "DELIVERED"));
    }

    @Test
    void rejectsInvalidAndTerminalStateChanges() {
        assertFalse(OrderService.isAllowedTransition("DELIVERED", "PROCESSING"));
        assertFalse(OrderService.isAllowedTransition("CANCELLED", "PROCESSING"));
        assertFalse(OrderService.isAllowedTransition("SHIPPING", "CANCELLED"));
        assertFalse(OrderService.isAllowedTransition("PENDING", "DELIVERED"));
        assertFalse(OrderService.isAllowedTransition("PENDING", "UNKNOWN"));
    }
}
