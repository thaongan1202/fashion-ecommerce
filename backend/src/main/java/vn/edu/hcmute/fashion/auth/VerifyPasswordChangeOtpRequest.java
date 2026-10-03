package vn.edu.hcmute.fashion.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordChangeOtpRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm 6 chữ số")
        String otp
) {}
