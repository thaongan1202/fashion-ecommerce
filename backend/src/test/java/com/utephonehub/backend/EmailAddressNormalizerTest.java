package com.utephonehub.backend;

import com.utephonehub.backend.util.EmailAddressNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailAddressNormalizerTest {

    @Test
    void normalizesGmailDotsAndPlusTags() {
        assertEquals("lamngoc@gmail.com",
                EmailAddressNormalizer.normalize("  Lam.Ngoc+shop@gmail.com  "));
    }

    @Test
    void treatsGooglemailAsGmail() {
        assertEquals("lamngoc@gmail.com",
                EmailAddressNormalizer.normalize("lam.ngoc@googlemail.com"));
    }

    @Test
    void preservesDotsAndPlusTagsForOtherProviders() {
        assertEquals("lam.ngoc+shop@example.com",
                EmailAddressNormalizer.normalize(" Lam.Ngoc+Shop@Example.com "));
    }
}
