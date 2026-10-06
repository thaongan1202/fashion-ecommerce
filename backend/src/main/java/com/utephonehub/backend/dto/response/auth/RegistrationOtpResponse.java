package com.utephonehub.backend.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationOtpResponse {
    private String email;
    private int expiresInSeconds;
    private int maxAttempts;
    private int remainingAttempts;
}
