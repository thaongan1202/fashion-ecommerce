package com.utephonehub.backend;

import com.utephonehub.backend.util.VietnameseMobilePhoneValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VietnameseMobilePhoneValidatorTest {

    @Test
    void acceptsVietnameseMobileNumbersWithAssignedPrefixes() {
        assertTrue(VietnameseMobilePhoneValidator.isValid("0912345678"));
        assertTrue(VietnameseMobilePhoneValidator.isValid("0321234567"));
        assertTrue(VietnameseMobilePhoneValidator.isValid("0551234567"));
        assertTrue(VietnameseMobilePhoneValidator.isValid("0871234567"));
    }

    @Test
    void rejectsInvalidLengthAndUnassignedPrefixes() {
        assertFalse(VietnameseMobilePhoneValidator.isValid("123456789"));
        assertFalse(VietnameseMobilePhoneValidator.isValid("091234567"));
        assertFalse(VietnameseMobilePhoneValidator.isValid("09123456789"));
        assertFalse(VietnameseMobilePhoneValidator.isValid("0121234567"));
        assertFalse(VietnameseMobilePhoneValidator.isValid(" 0912345678"));
        assertFalse(VietnameseMobilePhoneValidator.isValid(null));
    }
}
