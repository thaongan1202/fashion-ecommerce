package vn.edu.hcmute.fashion.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        @Email
        @Size(max = 254)
        @Pattern(
                regexp = "(?i)^[^@\\s]+@gmail\\.com$",
                message = "Email phải có dạng @gmail.com"
        )
        String email,

        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm 6 chữ số")
        String otp,

        @NotBlank
        @Size(min = 8, max = 32)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).*$",
                message = "Mật khẩu cần có chữ hoa, chữ thường, số và ký tự đặc biệt"
        )
        String newPassword
) {}
