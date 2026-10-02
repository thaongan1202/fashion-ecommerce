package vn.edu.hcmute.fashion.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank
        @Email
        @Size(max = 254)
        @Pattern(
                regexp = "(?i)^[^@\\s]+@gmail\\.com$",
                message = "Email phải có dạng @gmail.com"
        )
        String email
) {}
