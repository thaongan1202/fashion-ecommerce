package vn.edu.hcmute.fashion.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyRegistrationOtpRequest(
        @NotBlank
        @Email
        @Pattern(regexp = "(?i)^[^@\\s]+@gmail\\.com$", message = "Email phải có dạng @gmail.com")
        String email,

        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm 6 chữ số")
        String otp
) {}
